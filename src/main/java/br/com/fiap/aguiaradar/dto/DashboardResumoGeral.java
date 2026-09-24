package br.com.fiap.aguiaradar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumoGeral {
    private long totalProjetos;
    private long projetosConcluidos;
    private long projetosEmAndamento;
    private double investimentoTotal;
    private double retornoTotal;
    private double lucroTotal;
    private double roiMedioPercentual;
    private double aumentoProdutividadeMedioPercentual;
}
