package br.com.fiap.aguiaradar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiaradar.model.enums.StatusProjeto;

@Data
public class ProjetoRequest {
    @NotBlank
    private String titulo;

    private String descricao;

    private String ideiaOrigemId;

    private String orientacaoEstrategicaId;

    private String etapa;

    private StatusProjeto status;

    private Double investimento;

    private LocalDate prazo;

    private Double retornoFinanceiro;

    private Double roiPercentual;

    private Double aumentoProdutividadePercentual;

    private List<String> resultadosObtidos;
}
