package br.com.fiap.aguiaradar.controller;

import br.com.fiap.aguiaradar.dto.AvaliacaoIdeiaRequest;
import br.com.fiap.aguiaradar.dto.IdeiaRequest;
import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.model.Usuario;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import br.com.fiap.aguiaradar.security.AutenticacaoUtil;
import br.com.fiap.aguiaradar.service.IdeiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ideias de inovacao / problemas enfrentados pelos operadores.
 * - OPERADOR: CRUD das proprias ideias.
 * - GESTOR/LIDERANCA: consulta todas; GESTOR pode priorizar/aprovar.
 */
@RestController
@RequestMapping("/api/v1/ideias")
@RequiredArgsConstructor
public class IdeiaController {

    private final IdeiaService service;
    private final AutenticacaoUtil autenticacaoUtil;

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','LIDERANCA')")
    public ResponseEntity<List<Ideia>> listarTodas(@RequestParam(required = false) StatusIdeia status) {
        return ResponseEntity.ok(status != null ? service.listarPorStatus(status) : service.listarTodas());
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<Ideia>> minhasIdeias() {
        Usuario logado = autenticacaoUtil.usuarioLogado();
        return ResponseEntity.ok(service.listarPorColaborador(logado.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ideia> buscar(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Ideia> criar(@Valid @RequestBody IdeiaRequest request) {
        Ideia criada = service.criar(request, autenticacaoUtil.usuarioLogado());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Ideia> atualizar(@PathVariable String id, @Valid @RequestBody IdeiaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request, autenticacaoUtil.usuarioLogado()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.remover(id, autenticacaoUtil.usuarioLogado());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/avaliar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Ideia> avaliar(@PathVariable String id, @Valid @RequestBody AvaliacaoIdeiaRequest request) {
        return ResponseEntity.ok(service.avaliar(id, request, autenticacaoUtil.usuarioLogado()));
    }

    @PutMapping("/{id}/priorizar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Ideia> priorizar(@PathVariable String id, @RequestParam int prioridade) {
        return ResponseEntity.ok(service.priorizar(id, prioridade));
    }
}
