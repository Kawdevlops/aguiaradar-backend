package br.com.fiap.aguiaradar.dto;

import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPorEstrategia {
    private String orientacaoEstrategicaId;
    private CategoriaIdeia categoria;
    private String campanha;
    private long totalProjetos;
    private double investimentoTotal;
    private double retornoTotal;
    private double roiMedioPercentual;
}
