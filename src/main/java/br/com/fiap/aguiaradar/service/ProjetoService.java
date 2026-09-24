package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.ProjetoRequest;
import br.com.fiap.aguiaradar.exception.RecursoNaoEncontradoException;
import br.com.fiap.aguiaradar.model.Projeto;
import br.com.fiap.aguiaradar.model.enums.StatusProjeto;
import br.com.fiap.aguiaradar.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras do desafio:
 * - GESTOR: cadastra os projetos/iniciativas, atualiza dados e adiciona resultados (CRUD completo).
 * - LIDERANCA: apenas consulta o andamento (etapa, status, investimento, prazo, retorno financeiro).
 * - Todo projeto pode ser vinculado a orientacao estrategica vigente e/ou a ideia de origem.
 */
@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository repository;

    public List<Projeto> listarTodos() {
        return repository.findAll();
    }

    public List<Projeto> listarPorStatus(StatusProjeto status) {
        return repository.findByStatus(status);
    }

    public List<Projeto> listarPorOrientacao(String orientacaoEstrategicaId) {
        return repository.findByOrientacaoEstrategicaId(orientacaoEstrategicaId);
    }

    public Projeto buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto nao encontrado: " + id));
    }

    public Projeto criar(ProjetoRequest request, String gestorId) {
        Projeto projeto = Projeto.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .ideiaOrigemId(request.getIdeiaOrigemId())
                .orientacaoEstrategicaId(request.getOrientacaoEstrategicaId())
                .etapa(request.getEtapa() != null ? request.getEtapa() : "Planejamento")
                .status(request.getStatus() != null ? request.getStatus() : StatusProjeto.PLANEJADO)
                .investimento(request.getInvestimento())
                .prazo(request.getPrazo())
                .retornoFinanceiro(request.getRetornoFinanceiro())
                .roiPercentual(request.getRoiPercentual())
                .aumentoProdutividadePercentual(request.getAumentoProdutividadePercentual())
                .resultadosObtidos(request.getResultadosObtidos())
                .gestorResponsavelId(gestorId)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        return repository.save(projeto);
    }

    public Projeto atualizar(String id, ProjetoRequest request) {
        Projeto existente = buscarPorId(id);

        existente.setTitulo(request.getTitulo());
        existente.setDescricao(request.getDescricao());
        existente.setIdeiaOrigemId(request.getIdeiaOrigemId());
        existente.setOrientacaoEstrategicaId(request.getOrientacaoEstrategicaId());
        if (request.getEtapa() != null) existente.setEtapa(request.getEtapa());
        if (request.getStatus() != null) existente.setStatus(request.getStatus());
        if (request.getInvestimento() != null) existente.setInvestimento(request.getInvestimento());
        if (request.getPrazo() != null) existente.setPrazo(request.getPrazo());
        if (request.getRetornoFinanceiro() != null) existente.setRetornoFinanceiro(request.getRetornoFinanceiro());
        if (request.getRoiPercentual() != null) existente.setRoiPercentual(request.getRoiPercentual());
        if (request.getAumentoProdutividadePercentual() != null) {
            existente.setAumentoProdutividadePercentual(request.getAumentoProdutividadePercentual());
        }
        if (request.getResultadosObtidos() != null) existente.setResultadosObtidos(request.getResultadosObtidos());

        existente.setDataAtualizacao(LocalDateTime.now());

        return repository.save(existente);
    }

    public Projeto adicionarResultado(String id, String resultado) {
        Projeto existente = buscarPorId(id);
        existente.getResultadosObtidos().add(resultado);
        existente.setDataAtualizacao(LocalDateTime.now());
        return repository.save(existente);
    }

    public void remover(String id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
