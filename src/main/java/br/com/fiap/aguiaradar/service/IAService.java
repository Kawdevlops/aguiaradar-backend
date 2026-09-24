package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import br.com.fiap.aguiaradar.repository.IdeiaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diferencial (Plus) do desafio: integracao com IA para pontuar e priorizar
 * as ideias/iniciativas dos colaboradores, auxiliando o gestor na selecao dos
 * futuros projetos, e para gerar insights sobre os resultados do dashboard.
 *
 * Utiliza uma API gratuita compativel com o padrao OpenAI/OpenRouter
 * (https://openrouter.ai - modelos ":free"), configurada via application.yml:
 *   aguiaradar.ia.provider-url
 *   aguiaradar.ia.api-key
 *   aguiaradar.ia.model
 *
 * Caso a chave nao seja configurada (aguiaradar.ia.enabled=false ou api-key vazia),
 * o servico cai automaticamente em um modo heuristico local (fallback), garantindo
 * que a funcionalidade continue operante mesmo sem acesso a internet/chave.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IAService {

    private final IdeiaRepository ideiaRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aguiaradar.ia.provider-url}")
    private String providerUrl;

    @Value("${aguiaradar.ia.api-key}")
    private String apiKey;

    @Value("${aguiaradar.ia.model}")
    private String model;

    @Value("${aguiaradar.ia.enabled:true}")
    private boolean iaEnabled;

    private static final Pattern PONTUACAO_PATTERN = Pattern.compile("PONTUACAO:\\s*([0-9]+(?:[\\.,][0-9]+)?)");
    private static final Pattern JUSTIFICATIVA_PATTERN = Pattern.compile("JUSTIFICATIVA:\\s*(.+)", Pattern.DOTALL);

    /**
     * Pontua e prioriza automaticamente todas as ideias PENDENTES,
     * atualizando pontuacaoIA, justificativaIA e status -> PRIORIZADA.
     */
    public List<Ideia> priorizarIdeiasPendentes() {
        List<Ideia> pendentes = ideiaRepository.findByStatus(StatusIdeia.PENDENTE);

        for (Ideia ideia : pendentes) {
            String justificativa = pontuarIdeia(ideia);
            ideia.setJustificativaIA(justificativa);
            ideia.setPontuacaoIA(extrairPontuacao(justificativa));
            ideia.setStatus(StatusIdeia.PRIORIZADA);
            ideiaRepository.save(ideia);
        }

        // reordena a prioridade das ideias priorizadas com base na pontuacao (maior pontuacao = prioridade 1)
        List<Ideia> priorizadas = ideiaRepository.findByStatus(StatusIdeia.PRIORIZADA);
        priorizadas.sort((a, b) -> Double.compare(
                b.getPontuacaoIA() != null ? b.getPontuacaoIA() : 0,
                a.getPontuacaoIA() != null ? a.getPontuacaoIA() : 0));

        for (int i = 0; i < priorizadas.size(); i++) {
            priorizadas.get(i).setPrioridade(i + 1);
        }
        ideiaRepository.saveAll(priorizadas);

        return priorizadas;
    }

    /** Pontua uma unica ideia sob demanda, sem alterar seu status. */
    public Ideia pontuarUmaIdeia(String ideiaId) {
        Ideia ideia = ideiaRepository.findById(ideiaId)
                .orElseThrow(() -> new br.com.fiap.aguiaradar.exception.RecursoNaoEncontradoException(
                        "Ideia nao encontrada: " + ideiaId));

        String resposta = pontuarIdeia(ideia);
        ideia.setJustificativaIA(resposta);
        ideia.setPontuacaoIA(extrairPontuacao(resposta));
        return ideiaRepository.save(ideia);
    }

    /** Gera um insight textual em cima dos numeros do dashboard (funcionalidade extra de IA). */
    public String gerarInsightDashboard(String resumoNumerico) {
        String prompt = "Voce e um analista de inovacao corporativa. Com base no resumo de "
                + "indicadores de projetos abaixo, escreva em portugues, em ate 4 frases, um insight "
                + "objetivo destacando pontos de atencao e sugestoes de melhoria para a lideranca.\n\n"
                + "Resumo: " + resumoNumerico;

        return chamarIA(prompt, "Nao foi possivel gerar o insight via IA no momento. "
                + "Resumo bruto: " + resumoNumerico);
    }

    private String pontuarIdeia(Ideia ideia) {
        String prompt = "Voce e um avaliador de ideias de inovacao corporativa de uma empresa de "
                + "transporte e logistica (Aguia Branca). Avalie a ideia abaixo e responda ESTRITAMENTE "
                + "no formato:\nPONTUACAO: <numero de 0 a 10>\nJUSTIFICATIVA: <ate 3 frases explicando "
                + "impacto, viabilidade e alinhamento estrategico>\n\n"
                + "Titulo: " + ideia.getTitulo() + "\n"
                + "Categoria: " + (ideia.getCategoria() != null ? ideia.getCategoria() : "Nao informada") + "\n"
                + "Descricao: " + ideia.getDescricao();

        return chamarIA(prompt, gerarFallbackHeuristico(ideia));
    }

    private String chamarIA(String prompt, String respostaFallback) {
        if (!iaEnabled || apiKey == null || apiKey.isBlank()) {
            log.info("IA desabilitada ou sem api-key configurada; utilizando fallback heuristico local.");
            return respostaFallback;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.3
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String respostaBruta = restTemplate.postForObject(providerUrl, entity, String.class);

            JsonNode raiz = objectMapper.readTree(respostaBruta);
            JsonNode conteudo = raiz.path("choices").path(0).path("message").path("content");

            if (conteudo.isMissingNode() || conteudo.asText().isBlank()) {
                return respostaFallback;
            }
            return conteudo.asText();

        } catch (Exception e) {
            log.warn("Falha ao chamar provedor de IA ({}). Usando fallback heuristico. Motivo: {}",
                    providerUrl, e.getMessage());
            return respostaFallback;
        }
    }

    /**
     * Fallback simples baseado em heuristica de texto (sem chamada externa),
     * usado quando a chave de IA nao esta configurada ou o provedor esta indisponivel,
     * garantindo que a funcionalidade de priorizacao continue demonstravel.
     */
    private String gerarFallbackHeuristico(Ideia ideia) {
        int tamanhoDescricao = ideia.getDescricao() != null ? ideia.getDescricao().length() : 0;
        boolean vinculadaEstrategia = ideia.getOrientacaoEstrategicaId() != null
                && !ideia.getOrientacaoEstrategicaId().isBlank();

        double pontuacao = 5.0;
        pontuacao += Math.min(tamanhoDescricao / 100.0, 3.0); // ideias mais detalhadas pontuam mais
        pontuacao += vinculadaEstrategia ? 2.0 : 0.0;
        pontuacao = Math.min(pontuacao, 10.0);

        return String.format(java.util.Locale.ROOT,
                "PONTUACAO: %.1f\nJUSTIFICATIVA: Avaliacao heuristica local (IA externa indisponivel). "
                        + "Considerou-se o nivel de detalhamento da descricao e o vinculo com a orientacao "
                        + "estrategica vigente (%s).",
                pontuacao, vinculadaEstrategia ? "vinculada" : "nao vinculada");
    }

    private Double extrairPontuacao(String respostaIA) {
        if (respostaIA == null) return 0.0;
        Matcher m = PONTUACAO_PATTERN.matcher(respostaIA);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(",", "."));
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
