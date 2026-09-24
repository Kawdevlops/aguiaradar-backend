package br.com.fiap.aguiaradar.config;

import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.model.OrientacaoEstrategica;
import br.com.fiap.aguiaradar.model.Projeto;
import br.com.fiap.aguiaradar.model.Usuario;
import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import br.com.fiap.aguiaradar.model.enums.Perfil;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import br.com.fiap.aguiaradar.model.enums.StatusProjeto;
import br.com.fiap.aguiaradar.repository.IdeiaRepository;
import br.com.fiap.aguiaradar.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiaradar.repository.ProjetoRepository;
import br.com.fiap.aguiaradar.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cria usuarios, orientacoes, ideias e projetos na primeira inicializacao para
 * facilitar os testes/demo do desafio, respeitando as regras de acesso por perfil.
 * Pode ser desabilitado via aguiaradar.seed.enabled=false.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final OrientacaoEstrategicaRepository orientacaoRepository;
    private final IdeiaRepository ideiaRepository;
    private final ProjetoRepository projetoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${aguiaradar.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        criarUsuariosSeNaoExistirem();
        criarOrientacoesSeNaoExistirem();
        criarIdeiasSeNaoExistirem();
        criarProjetosSeNaoExistirem();

        log.info("=== AguiaRadar: dados de demonstracao prontos ===");
    }

    private void criarUsuariosSeNaoExistirem() {
        criarSeUsuarioNaoExiste("Ana Operadora", "operador@aguiaradar.com", "123456", Perfil.OPERADOR, 1);
        criarSeUsuarioNaoExiste("Bruno Gestor", "gestor@aguiaradar.com", "123456", Perfil.GESTOR, 1);
        criarSeUsuarioNaoExiste("Carla Lideranca", "lideranca@aguiaradar.com", "123456", Perfil.LIDERANCA, null);
    }

    private void criarOrientacoesSeNaoExistirem() {
        if (orientacaoRepository.count() > 0) {
            return;
        }

        Usuario lideranca = usuarioRepository.findByEmailIgnoreCase("lideranca@aguiaradar.com")
                .orElseThrow(() -> new IllegalStateException("Usuario LIDERANCA nao encontrado para seed."));

        OrientacaoEstrategica orientacao1 = OrientacaoEstrategica.builder()
                .categoria(CategoriaIdeia.SUSTENTABILIDADE)
                .campanha("ESG 2026")
                .descricao("Redução de emissões, eficiência energética e responsabilidade ambiental.")
                .ativo(true)
                .criadoPorUsuarioId(lideranca.getId())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        OrientacaoEstrategica orientacao2 = OrientacaoEstrategica.builder()
                .categoria(CategoriaIdeia.PROCESSO)
                .campanha("Digitalização Operacional")
                .descricao("Automação de processos e melhoria na rastreabilidade digital.")
                .ativo(true)
                .criadoPorUsuarioId(lideranca.getId())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        orientacaoRepository.saveAll(List.of(orientacao1, orientacao2));
        log.info("Orientacoes estrategicas seed criadas");
    }

    private void criarIdeiasSeNaoExistirem() {
        if (ideiaRepository.count() > 0) {
            return;
        }

        Usuario operador = usuarioRepository.findByEmailIgnoreCase("operador@aguiaradar.com")
                .orElseThrow(() -> new IllegalStateException("Usuario OPERADOR nao encontrado para seed."));

        List<OrientacaoEstrategica> orientacoes = orientacaoRepository.findAll();

        Ideia ideia1 = Ideia.builder()
                .titulo("App para rastrear entregas em tempo real")
                .descricao("Monitoramento de atrasos e status das entregas por rota para reduzir falhas operacionais.")
                .categoria(CategoriaIdeia.OUTROS)
                .colaboradorId(operador.getId())
                .colaboradorNome(operador.getNome())
                .orientacaoEstrategicaId(orientacoes.get(0).getId())
                .status(StatusIdeia.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        Ideia ideia2 = Ideia.builder()
                .titulo("Redução de desperdício de carga")
                .descricao("Melhoria no processo de consolidação de cargas para reduzir perdas e custos operacionais.")
                .categoria(CategoriaIdeia.CUSTOS)
                .colaboradorId(operador.getId())
                .colaboradorNome(operador.getNome())
                .orientacaoEstrategicaId(orientacoes.get(1).getId())
                .status(StatusIdeia.APROVADA)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        ideiaRepository.saveAll(List.of(ideia1, ideia2));
        log.info("Ideias seed criadas");
    }

    private void criarProjetosSeNaoExistirem() {
        if (projetoRepository.count() > 0) {
            return;
        }

        Usuario gestor = usuarioRepository.findByEmailIgnoreCase("gestor@aguiaradar.com")
                .orElseThrow(() -> new IllegalStateException("Usuario GESTOR nao encontrado para seed."));

        List<Ideia> ideias = ideiaRepository.findAll();
        List<OrientacaoEstrategica> orientacoes = orientacaoRepository.findAll();

        Projeto projeto1 = Projeto.builder()
                .titulo("Plataforma de rastreio operacional")
                .descricao("Implantação de dashboard e alertas para monitorar entregas em tempo real.")
                .ideiaOrigemId(ideias.get(0).getId())
                .orientacaoEstrategicaId(orientacoes.get(1).getId())
                .etapa("PLANEJAMENTO")
                .status(StatusProjeto.EM_ANDAMENTO)
                .investimento(25000.0)
                .prazo(LocalDate.now().plusMonths(4))
                .retornoFinanceiro(40000.0)
                .roiPercentual(60.0)
                .aumentoProdutividadePercentual(18.0)
                .gestorResponsavelId(gestor.getId())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        Projeto projeto2 = Projeto.builder()
                .titulo("Programa de redução de desperdício")
                .descricao("Ajuste de roteirização e consolidação de cargas para reduzir perdas e custos.")
                .ideiaOrigemId(ideias.get(1).getId())
                .orientacaoEstrategicaId(orientacoes.get(0).getId())
                .etapa("EXECUCAO")
                .status(StatusProjeto.PLANEJADO)
                .investimento(18000.0)
                .prazo(LocalDate.now().plusMonths(6))
                .retornoFinanceiro(30000.0)
                .roiPercentual(66.0)
                .aumentoProdutividadePercentual(12.0)
                .gestorResponsavelId(gestor.getId())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        projetoRepository.saveAll(List.of(projeto1, projeto2));
        log.info("Projetos seed criados");
    }

    private void criarSeUsuarioNaoExiste(String nome, String email, String senha, Perfil perfil, Integer filialId) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash(passwordEncoder.encode(senha))
                .perfil(perfil)
                .filialId(filialId)
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario seed criado: {} ({})", email, perfil);
    }
}
