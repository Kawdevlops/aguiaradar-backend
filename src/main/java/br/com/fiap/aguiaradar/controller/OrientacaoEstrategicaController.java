package br.com.fiap.aguiaradar.controller;

import br.com.fiap.aguiaradar.dto.OrientacaoEstrategicaRequest;
import br.com.fiap.aguiaradar.model.OrientacaoEstrategica;
import br.com.fiap.aguiaradar.security.AutenticacaoUtil;
import br.com.fiap.aguiaradar.service.OrientacaoEstrategicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Orientacoes estrategicas: CRUD exclusivo da LIDERANCA; demais perfis apenas consultam.
 */
@RestController
@RequestMapping("/api/v1/orientacoes-estrategicas")
@RequiredArgsConstructor
public class OrientacaoEstrategicaController {

    private final OrientacaoEstrategicaService service;
    private final AutenticacaoUtil autenticacaoUtil;

    @GetMapping
    public ResponseEntity<List<OrientacaoEstrategica>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean somenteAtivas) {
        return ResponseEntity.ok(somenteAtivas ? service.listarAtivas() : service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrientacaoEstrategica> buscar(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('LIDERANCA')")
    public ResponseEntity<OrientacaoEstrategica> criar(@Valid @RequestBody OrientacaoEstrategicaRequest request) {
        OrientacaoEstrategica criada = service.criar(request, autenticacaoUtil.usuarioLogado().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDERANCA')")
    public ResponseEntity<OrientacaoEstrategica> atualizar(@PathVariable String id,
                                                            @Valid @RequestBody OrientacaoEstrategicaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDERANCA')")
    public ResponseEntity<Void> inativar(@PathVariable String id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
