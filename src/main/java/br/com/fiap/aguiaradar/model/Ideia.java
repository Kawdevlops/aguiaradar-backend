package br.com.fiap.aguiaradar.model;

import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ideias")
public class Ideia {

    @Id
    private String id;

    private String titulo;

    private String descricao;

    private CategoriaIdeia categoria;

    private String colaboradorId;

    private String colaboradorNome;

    private String orientacaoEstrategicaId;

    private StatusIdeia status;

    private Integer prioridade; // definida pelo gestor (1 = maior prioridade)

    // Preenchidos pela integracao com IA (diferencial)
    private Double pontuacaoIA;
    private String justificativaIA;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    private String avaliadoPorUsuarioId;
    private LocalDateTime dataAvaliacao;
}
