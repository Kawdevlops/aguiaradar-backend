package br.com.fiap.aguiaradar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;

import java.time.LocalDateTime;

/**
 * Orientacao estrategica definida pela lideranca (CRUD exclusivo de LIDERANCA).
 * Mantem historico: cada registro possui id, data, categoria e campanha,
 * e registros antigos nao sao apagados fisicamente ao serem "desativados".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orientacoes_estrategicas")
public class OrientacaoEstrategica {

    @Id
    private String id;

    private CategoriaIdeia categoria;

    private String campanha;

    private String descricao;

    private boolean ativo;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    private String criadoPorUsuarioId;
}
