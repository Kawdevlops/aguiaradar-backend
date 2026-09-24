package br.com.fiap.aguiaradar.repository;

import br.com.fiap.aguiaradar.model.Ideia;
import br.com.fiap.aguiaradar.model.enums.StatusIdeia;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {
    List<Ideia> findByColaboradorId(String colaboradorId);
    List<Ideia> findByStatus(StatusIdeia status);
    List<Ideia> findByOrientacaoEstrategicaId(String orientacaoEstrategicaId);
}
