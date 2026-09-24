package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.OrientacaoEstrategicaRequest;
import br.com.fiap.aguiaradar.exception.RecursoNaoEncontradoException;
import br.com.fiap.aguiaradar.model.OrientacaoEstrategica;
import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import br.com.fiap.aguiaradar.repository.OrientacaoEstrategicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do CRUD de orientacoes estrategicas: criacao, atualizacao
 * e a regra de "exclusao" que apenas inativa o registro (mantendo historico).
 */
@ExtendWith(MockitoExtension.class)
class OrientacaoEstrategicaServiceTest {

    @Mock
    private OrientacaoEstrategicaRepository repository;

    @InjectMocks
    private OrientacaoEstrategicaService service;

    @Test
    void deveCriarOrientacaoAtivaPorPadrao() {
        OrientacaoEstrategicaRequest request = new OrientacaoEstrategicaRequest();
        request.setCategoria(CategoriaIdeia.SUSTENTABILIDADE);
        request.setCampanha("Campanha Verde 2026");
        request.setDescricao("Reduzir emissoes da frota");

        when(repository.save(any(OrientacaoEstrategica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrientacaoEstrategica criada = service.criar(request, "usuario-lideranca-1");

        assertThat(criada.isAtivo()).isTrue();
        assertThat(criada.getCampanha()).isEqualTo("Campanha Verde 2026");
        assertThat(criada.getCriadoPorUsuarioId()).isEqualTo("usuario-lideranca-1");
        assertThat(criada.getDataCriacao()).isNotNull();
    }

    @Test
    void inativarNaoDeveApagarORegistroApenasMudarAtivoParaFalse() {
        OrientacaoEstrategica existente = OrientacaoEstrategica.builder()
                .id("orient-1")
                .categoria(CategoriaIdeia.CUSTOS)
                .campanha("Reducao de custos logisticos")
                .ativo(true)
                .build();

        when(repository.findById("orient-1")).thenReturn(Optional.of(existente));
        when(repository.save(any(OrientacaoEstrategica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.inativar("orient-1");

        ArgumentCaptor<OrientacaoEstrategica> captor = ArgumentCaptor.forClass(OrientacaoEstrategica.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().isAtivo()).isFalse();
        assertThat(captor.getValue().getId()).isEqualTo("orient-1"); // registro preservado, so inativado
    }

    @Test
    void deveLancarExcecaoAoBuscarOrientacaoInexistente() {
        when(repository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("nao-existe"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
