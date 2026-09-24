package br.com.fiap.aguiaradar.dto;

import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IdeiaRequest {
    @NotBlank
    private String titulo;

    @NotBlank
    private String descricao;

    @NotNull
    private CategoriaIdeia categoria;

    private String orientacaoEstrategicaId;
}
