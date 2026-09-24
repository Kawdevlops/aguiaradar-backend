package br.com.fiap.aguiaradar.controller;

import br.com.fiap.aguiaradar.dto.DashboardPorEstrategia;
import br.com.fiap.aguiaradar.dto.DashboardResumoGeral;
import br.com.fiap.aguiaradar.dto.InsightIAResponse;
import br.com.fiap.aguiaradar.service.DashboardService;
import br.com.fiap.aguiaradar.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de relatorios resumidos consumidos pelo Dashboard (perfil LIDERANCA).
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final IAService iaService;

    @GetMapping("/resumo-geral")
    @PreAuthorize("hasAnyRole('LIDERANCA','GESTOR')")
    public ResponseEntity<DashboardResumoGeral> resumoGeral() {
        return ResponseEntity.ok(dashboardService.resumoGeral());
    }

    @GetMapping("/por-estrategia")
    @PreAuthorize("hasAnyRole('LIDERANCA','GESTOR')")
    public ResponseEntity<List<DashboardPorEstrategia>> porEstrategia() {
        return ResponseEntity.ok(dashboardService.resumoPorEstrategia());
    }

    /** Diferencial de IA: gera um insight textual sobre os numeros atuais do dashboard. */
    @GetMapping("/insight-ia")
    @PreAuthorize("hasRole('LIDERANCA')")
    public ResponseEntity<InsightIAResponse> insightIA() {
        DashboardResumoGeral resumo = dashboardService.resumoGeral();
        String resumoTexto = String.format(java.util.Locale.ROOT,
                "totalProjetos=%d, investimentoTotal=%.2f, retornoTotal=%.2f, lucroTotal=%.2f, "
                        + "roiMedio=%.2f%%, aumentoProdutividadeMedio=%.2f%%",
                resumo.getTotalProjetos(), resumo.getInvestimentoTotal(), resumo.getRetornoTotal(),
                resumo.getLucroTotal(), resumo.getRoiMedioPercentual(), resumo.getAumentoProdutividadeMedioPercentual());

        String insight = iaService.gerarInsightDashboard(resumoTexto);
        return ResponseEntity.ok(InsightIAResponse.builder().insight(insight).geradoPorIA(true).build());
    }
}
