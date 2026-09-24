package br.com.fiap.aguiaradar.controller;

import br.com.fiap.aguiaradar.dto.ProjetoRequest;
import br.com.fiap.aguiaradar.model.Projeto;
import br.com.fiap.aguiaradar.model.enums.StatusProjeto;
import br.com.fiap.aguiaradar.security.AutenticacaoUtil;
import br.com.fiap.aguiaradar.service.ProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Projetos e iniciativas.
 * - GESTOR: CRUD completo (cadastra, atualiza progresso, adiciona resultados).
 * - LIDERANCA/GESTOR: consulta o andamento (etapa, status, investimento, prazo, retorno).
 */
@RestController
@RequestMapping("/api/v1/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService service;
    private final AutenticacaoUtil autenticacaoUtil;

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','LIDERANCA')")
    public ResponseEntity<List<Projeto>> listar(@RequestParam(required = false) StatusProjeto status,
                                                 @RequestParam(required = false) String orientacaoEstrategicaId) {
        if (status != null) {
            return ResponseEntity.ok(service.listarPorStatus(status));
        }
        if (orientacaoEstrategicaId != null) {
            return ResponseEntity.ok(service.listarPorOrientacao(orientacaoEstrategicaId));
        }
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','LIDERANCA')")
    public ResponseEntity<Projeto> buscar(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Projeto> criar(@Valid @RequestBody ProjetoRequest request) {
        Projeto criado = service.criar(request, autenticacaoUtil.usuarioLogado().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Projeto> atualizar(@PathVariable String id, @Valid @RequestBody ProjetoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/resultados")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Projeto> adicionarResultado(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.adicionarResultado(id, body.get("resultado")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
