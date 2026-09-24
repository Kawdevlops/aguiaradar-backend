package br.com.fiap.aguiaradar.dto;

import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvaliacaoIdeiaRequest {
    @NotNull
    private StatusIdeia status; // APROVADA ou REPROVADA

    private Integer prioridade;
}
