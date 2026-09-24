package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.DashboardPorEstrategia;
import br.com.fiap.aguiaradar.dto.DashboardResumoGeral;
import br.com.fiap.aguiaradar.model.OrientacaoEstrategica;
import br.com.fiap.aguiaradar.model.Projeto;
import br.com.fiap.aguiaradar.model.enums.StatusProjeto;
import br.com.fiap.aguiaradar.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiaradar.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints de relatorios/agregacoes consumidos pelo dashboard do app
 * (perfil LIDERANCA): resumo geral (ROI, lucro, investimento, prazo,
 * aumento de produtividade) e quebra por orientacao estrategica/projeto.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjetoRepository projetoRepository;
    private final OrientacaoEstrategicaRepository orientacaoRepository;

    public DashboardResumoGeral resumoGeral() {
        List<Projeto> projetos = projetoRepository.findAll();

        long total = projetos.size();
        long concluidos = projetos.stream().filter(p -> p.getStatus() == StatusProjeto.CONCLUIDO).count();
        long emAndamento = projetos.stream().filter(p -> p.getStatus() == StatusProjeto.EM_ANDAMENTO).count();

        double investimentoTotal = somaOuZero(projetos, Projeto::getInvestimento);
        double retornoTotal = somaOuZero(projetos, Projeto::getRetornoFinanceiro);
        double lucroTotal = retornoTotal - investimentoTotal;
        double roiMedio = mediaOuZero(projetos, Projeto::getRoiPercentual);
        double produtividadeMedia = mediaOuZero(projetos, Projeto::getAumentoProdutividadePercentual);

        return DashboardResumoGeral.builder()
                .totalProjetos(total)
                .projetosConcluidos(concluidos)
                .projetosEmAndamento(emAndamento)
                .investimentoTotal(investimentoTotal)
                .retornoTotal(retornoTotal)
                .lucroTotal(lucroTotal)
                .roiMedioPercentual(roiMedio)
                .aumentoProdutividadeMedioPercentual(produtividadeMedia)
                .build();
    }

    public List<DashboardPorEstrategia> resumoPorEstrategia() {
        List<Projeto> projetos = projetoRepository.findAll();
        Map<String, OrientacaoEstrategica> orientacoesPorId = orientacaoRepository.findAll().stream()
                .collect(Collectors.toMap(OrientacaoEstrategica::getId, o -> o));

        Map<String, List<Projeto>> projetosPorEstrategia = projetos.stream()
                .filter(p -> p.getOrientacaoEstrategicaId() != null)
                .collect(Collectors.groupingBy(Projeto::getOrientacaoEstrategicaId));

        return projetosPorEstrategia.entrySet().stream()
                .map(entry -> {
                    String orientacaoId = entry.getKey();
                    List<Projeto> lista = entry.getValue();
                    OrientacaoEstrategica orientacao = orientacoesPorId.get(orientacaoId);

                    return DashboardPorEstrategia.builder()
                            .orientacaoEstrategicaId(orientacaoId)
                            .categoria(orientacao != null ? orientacao.getCategoria() : null)
                            .campanha(orientacao != null ? orientacao.getCampanha() : "N/A")
                            .totalProjetos(lista.size())
                            .investimentoTotal(somaOuZero(lista, Projeto::getInvestimento))
                            .retornoTotal(somaOuZero(lista, Projeto::getRetornoFinanceiro))
                            .roiMedioPercentual(mediaOuZero(lista, Projeto::getRoiPercentual))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double somaOuZero(List<Projeto> projetos, java.util.function.Function<Projeto, Double> extrator) {
        return projetos.stream()
                .map(extrator)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private double mediaOuZero(List<Projeto> projetos, java.util.function.Function<Projeto, Double> extrator) {
        return projetos.stream()
                .map(extrator)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
