package br.com.fiap.aguiaradar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import br.com.fiap.aguiaradar.model.enums.StatusProjeto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projetos")
public class Projeto {

    @Id
    private String id;

    private String titulo;

    private String descricao;

    private String ideiaOrigemId;

    private String orientacaoEstrategicaId;

    private String etapa; // ex: Planejamento, Execucao, Testes, Entrega

    private StatusProjeto status;

    private Double investimento;

    private LocalDate prazo;

    private Double retornoFinanceiro;

    private Double roiPercentual;

    private Double aumentoProdutividadePercentual;

    @Builder.Default
    private List<String> resultadosObtidos = new ArrayList<>();

    private String gestorResponsavelId;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;
}
