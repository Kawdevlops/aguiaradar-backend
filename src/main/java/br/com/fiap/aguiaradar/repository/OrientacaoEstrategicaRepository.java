package br.com.fiap.aguiaradar.repository;

import br.com.fiap.aguiaradar.model.OrientacaoEstrategica;
import br.com.fiap.aguiaradar.model.enums.CategoriaIdeia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrientacaoEstrategicaRepository extends MongoRepository<OrientacaoEstrategica, String> {
    List<OrientacaoEstrategica> findByAtivoTrue();
    List<OrientacaoEstrategica> findByCategoria(CategoriaIdeia categoria);
}
