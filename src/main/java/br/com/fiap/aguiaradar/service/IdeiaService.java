package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiaradar.dto.IdeiaRequest;
import br.com.fiap.aguiaradar.exception.RecursoNaoEncontradoException;
import br.com.fiap.aguiaradar.exception.RegraDeNegocioException;
import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.model.Usuario;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import br.com.fiap.aguiaradar.repository.IdeiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras do desafio:
 * - OPERADOR: cadastra e consulta suas proprias ideias (CRUD do proprio conteudo).
 * - GESTOR: consulta, prioriza e aprova/reprova as ideias.
 * - Toda ideia pode ser vinculada a orientacao estrategica vigente.
 */
@Service
@RequiredArgsConstructor
public class IdeiaService {

    private final IdeiaRepository repository;

    public List<Ideia> listarTodas() {
        return repository.findAll();
    }

    public List<Ideia> listarPorColaborador(String colaboradorId) {
        return repository.findByColaboradorId(colaboradorId);
    }

    public List<Ideia> listarPorStatus(StatusIdeia status) {
        return repository.findByStatus(status);
    }

    public Ideia buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia nao encontrada: " + id));
    }

    public Ideia criar(IdeiaRequest request, Usuario colaborador) {
        Ideia ideia = Ideia.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .categoria(request.getCategoria())
                .orientacaoEstrategicaId(request.getOrientacaoEstrategicaId())
                .colaboradorId(colaborador.getId())
                .colaboradorNome(colaborador.getNome())
                .status(StatusIdeia.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        return repository.save(ideia);
    }

    public Ideia atualizar(String id, IdeiaRequest request, Usuario solicitante) {
        Ideia existente = buscarPorId(id);
        validarDonoDaIdeia(existente, solicitante);

        if (existente.getStatus() != StatusIdeia.PENDENTE) {
            throw new RegraDeNegocioException("Nao e possivel editar uma ideia que ja foi avaliada.");
        }

        existente.setTitulo(request.getTitulo());
        existente.setDescricao(request.getDescricao());
        existente.setCategoria(request.getCategoria());
        existente.setOrientacaoEstrategicaId(request.getOrientacaoEstrategicaId());
        existente.setDataAtualizacao(LocalDateTime.now());

        return repository.save(existente);
    }

    public void remover(String id, Usuario solicitante) {
        Ideia existente = buscarPorId(id);
        validarDonoDaIdeia(existente, solicitante);
        repository.deleteById(id);
    }

    public Ideia avaliar(String id, AvaliacaoIdeiaRequest request, Usuario gestor) {
        Ideia existente = buscarPorId(id);

        if (request.getStatus() != StatusIdeia.APROVADA && request.getStatus() != StatusIdeia.REPROVADA) {
            throw new RegraDeNegocioException("Status de avaliacao invalido. Utilize APROVADA ou REPROVADA.");
        }

        existente.setStatus(request.getStatus());
        if (request.getPrioridade() != null) {
            existente.setPrioridade(request.getPrioridade());
        }
        existente.setAvaliadoPorUsuarioId(gestor.getId());
        existente.setDataAvaliacao(LocalDateTime.now());
        existente.setDataAtualizacao(LocalDateTime.now());

        return repository.save(existente);
    }

    public Ideia priorizar(String id, int prioridade) {
        Ideia existente = buscarPorId(id);
        existente.setPrioridade(prioridade);
        existente.setStatus(StatusIdeia.PRIORIZADA);
        existente.setDataAtualizacao(LocalDateTime.now());
        return repository.save(existente);
    }

    private void validarDonoDaIdeia(Ideia ideia, Usuario solicitante) {
        boolean ehGestorOuLideranca = solicitante.getPerfil().name().equals("GESTOR")
                || solicitante.getPerfil().name().equals("LIDERANCA");

        if (!ideia.getColaboradorId().equals(solicitante.getId()) && !ehGestorOuLideranca) {
            throw new RegraDeNegocioException("Voce so pode alterar as ideias que voce mesmo cadastrou.");
        }
    }
}
