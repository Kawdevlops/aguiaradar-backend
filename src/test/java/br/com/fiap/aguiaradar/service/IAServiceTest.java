package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import br.com.fiap.aguiaradar.repository.IdeiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes do diferencial de IA (pontuacao/priorizacao de ideias). Como a
 * api-key nao e configurada nestes testes, o IAService cai automaticamente
 * no fallback heuristico local (sem chamada HTTP real) - o que o torna
 * 100% determinístico e testável sem rede.
 */
@ExtendWith(MockitoExtension.class)
class IAServiceTest {

    @Mock
    private IdeiaRepository ideiaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private IAService iaService;

    @BeforeEach
    void setUp() {
        // Simula application.yml com IA_API_KEY vazia -> forca o modo fallback heuristico.
        ReflectionTestUtils.setField(iaService, "providerUrl", "https://openrouter.ai/api/v1/chat/completions");
        ReflectionTestUtils.setField(iaService, "apiKey", "");
        ReflectionTestUtils.setField(iaService, "model", "meta-llama/llama-3.1-8b-instruct:free");
        ReflectionTestUtils.setField(iaService, "iaEnabled", true);
    }

    @Test
    void deveGerarPontuacaoHeuristicaQuandoApiKeyNaoConfigurada() {
        Ideia ideia = Ideia.builder()
                .id("ideia-1")
                .titulo("Check digital no patio")
                .descricao("Formulario digital reduzindo 20 minutos por turno em media.")
                .categoria(CategoriaIdeia.PROCESSO)
                .orientacaoEstrategicaId("orient-1") // vinculada -> pontuacao maior
                .status(StatusIdeia.PENDENTE)
                .build();

        when(ideiaRepository.findById("ideia-1")).thenReturn(java.util.Optional.of(ideia));
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));

        Ideia resultado = iaService.pontuarUmaIdeia("ideia-1");

        assertThat(resultado.getPontuacaoIA()).isNotNull();
        assertThat(resultado.getPontuacaoIA()).isBetween(0.0, 10.0);
        assertThat(resultado.getJustificativaIA()).contains("heuristica local");
    }

    @Test
    void ideiaVinculadaAEstrategiaDevePontuarMaisQueIdeiaSemVinculo() {
        Ideia vinculada = Ideia.builder()
                .id("v1").titulo("t").descricao("descricao curta")
                .orientacaoEstrategicaId("orient-1").status(StatusIdeia.PENDENTE).build();
        Ideia semVinculo = Ideia.builder()
                .id("v2").titulo("t").descricao("descricao curta")
                .orientacaoEstrategicaId(null).status(StatusIdeia.PENDENTE).build();

        when(ideiaRepository.findById("v1")).thenReturn(java.util.Optional.of(vinculada));
        when(ideiaRepository.findById("v2")).thenReturn(java.util.Optional.of(semVinculo));
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));

        double pontuacaoVinculada = iaService.pontuarUmaIdeia("v1").getPontuacaoIA();
        double pontuacaoSemVinculo = iaService.pontuarUmaIdeia("v2").getPontuacaoIA();

        assertThat(pontuacaoVinculada).isGreaterThan(pontuacaoSemVinculo);
    }

    @Test
    void deveReordenarPrioridadeDasIdeiasPelaPontuacao() {
        Ideia baixa = Ideia.builder().id("baixa").titulo("t").descricao("curta")
                .status(StatusIdeia.PENDENTE).build();
        Ideia alta = Ideia.builder().id("alta").titulo("t")
                .descricao("uma descricao bem mais detalhada e completa sobre o impacto esperado da ideia")
                .orientacaoEstrategicaId("orient-1")
                .status(StatusIdeia.PENDENTE).build();

        when(ideiaRepository.findByStatus(StatusIdeia.PENDENTE)).thenReturn(List.of(baixa, alta));
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ideiaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        // lista mutavel: o service reordena (sort) a lista retornada pelo repositorio
        when(ideiaRepository.findByStatus(StatusIdeia.PRIORIZADA))
                .thenReturn(new java.util.ArrayList<>(List.of(baixa, alta)));

        List<Ideia> priorizadas = iaService.priorizarIdeiasPendentes();

        assertThat(priorizadas.get(0).getPrioridade()).isEqualTo(1);
        assertThat(priorizadas.get(0).getPontuacaoIA())
                .isGreaterThanOrEqualTo(priorizadas.get(1).getPontuacaoIA());
        assertThat(priorizadas).allSatisfy(i -> assertThat(i.getStatus()).isEqualTo(StatusIdeia.PRIORIZADA));
    }
}
