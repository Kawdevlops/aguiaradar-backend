package br.com.fiap.aguiaradar.repository;

import br.com.fiap.aguiaradar.model.Projeto;
import br.com.fiap.aguiaradar.model.enums.StatusProjeto;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {
    List<Projeto> findByStatus(StatusProjeto status);
    List<Projeto> findByOrientacaoEstrategicaId(String orientacaoEstrategicaId);
    List<Projeto> findByGestorResponsavelId(String gestorResponsavelId);
}
