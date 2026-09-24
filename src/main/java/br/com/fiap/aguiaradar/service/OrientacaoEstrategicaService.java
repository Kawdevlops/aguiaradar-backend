package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.OrientacaoEstrategicaRequest;
import br.com.fiap.aguiaradar.exception.RecursoNaoEncontradoException;
import br.com.fiap.aguiaradar.model.OrientacaoEstrategica;
import br.com.fiap.aguiaradar.repository.OrientacaoEstrategicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CRUD das orientacoes estrategicas. Regra do desafio:
 * - LIDERANCA: cria/edita/remove (checado via @PreAuthorize no controller).
 * - demais perfis: apenas consulta (metodos de leitura, sem restricao de perfil aqui).
 * Mantemos historico: "excluir" apenas inativa o registro, nunca apaga fisicamente.
 */
@Service
@RequiredArgsConstructor
public class OrientacaoEstrategicaService {

    private final OrientacaoEstrategicaRepository repository;

    public List<OrientacaoEstrategica> listarTodas() {
        return repository.findAll();
    }

    public List<OrientacaoEstrategica> listarAtivas() {
        return repository.findByAtivoTrue();
    }

    public OrientacaoEstrategica buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orientacao estrategica nao encontrada: " + id));
    }

    public OrientacaoEstrategica criar(OrientacaoEstrategicaRequest request, String usuarioId) {
        OrientacaoEstrategica orientacao = OrientacaoEstrategica.builder()
                .categoria(request.getCategoria())
                .campanha(request.getCampanha())
                .descricao(request.getDescricao())
                .ativo(request.getAtivo() == null || request.getAtivo())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .criadoPorUsuarioId(usuarioId)
                .build();

        return repository.save(orientacao);
    }

    public OrientacaoEstrategica atualizar(String id, OrientacaoEstrategicaRequest request) {
        OrientacaoEstrategica existente = buscarPorId(id);

        existente.setCategoria(request.getCategoria());
        existente.setCampanha(request.getCampanha());
        existente.setDescricao(request.getDescricao());
        if (request.getAtivo() != null) {
            existente.setAtivo(request.getAtivo());
        }
        existente.setDataAtualizacao(LocalDateTime.now());

        return repository.save(existente);
    }

    public void inativar(String id) {
        OrientacaoEstrategica existente = buscarPorId(id);
        existente.setAtivo(false);
        existente.setDataAtualizacao(LocalDateTime.now());
        repository.save(existente);
    }
}
