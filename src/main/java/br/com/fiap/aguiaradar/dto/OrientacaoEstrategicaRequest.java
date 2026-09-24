package br.com.fiap.aguiaradar.dto;

import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrientacaoEstrategicaRequest {
    @NotBlank
    private CategoriaIdeia categoria;

    @NotBlank
    private String campanha;

    private String descricao;

    private Boolean ativo;
}
