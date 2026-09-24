package br.com.fiap.aguiaradar.controller;

import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Diferencial (Plus) do desafio: integracao com IA para pontuacao/priorizacao
 * automatica das ideias de inovacao, auxiliando o GESTOR na selecao dos
 * futuros projetos.
 */
@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
public class IAController {

    private final IAService iaService;

    /** Roda a IA sobre todas as ideias PENDENTES, pontuando e reordenando por prioridade. */
    @PostMapping("/priorizar-ideias")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<List<Ideia>> priorizarIdeiasPendentes() {
        return ResponseEntity.ok(iaService.priorizarIdeiasPendentes());
    }

    /** Roda a IA sobre uma unica ideia, sem alterar seu status atual. */
    @PostMapping("/ideias/{id}/pontuar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Ideia> pontuarIdeia(@PathVariable String id) {
        return ResponseEntity.ok(iaService.pontuarUmaIdeia(id));
    }
}
