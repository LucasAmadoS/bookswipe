package com.bookswipe.service;

import com.bookswipe.dao.EstanteDAO;
import com.bookswipe.dao.IndicadorDAO;
import com.bookswipe.dao.SolicitacaoTrocaDAO;
import com.bookswipe.model.Contagem;
import com.bookswipe.model.StatusLeitura;

import java.sql.SQLException;
import java.util.List;

/**
 * Os números das telas "Meu impacto" e do dashboard.
 * O DAO busca as contagens; aqui ficam as contas (porcentagens e médias).
 */
public class IndicadorService {

    private final IndicadorDAO indicadorDAO = new IndicadorDAO();
    private final SolicitacaoTrocaDAO solicitacaoDAO = new SolicitacaoTrocaDAO();
    private final EstanteDAO estanteDAO = new EstanteDAO();

    // Os números do perfil e do "Meu impacto" de uma pessoa
    public record ImpactoUsuario(int livrosLidos, int favoritos, int livrosReutilizados) {
    }

    // ♻️ Indicador principal: livros que voltaram a circular
    public int livrosReutilizados() throws SQLException {
        return solicitacaoDAO.contarConcluidas();
    }

    // Taxa de reuso (%) = trocas concluídas / livros oferecidos
    public int taxaReuso() throws SQLException {
        int oferecidos = indicadorDAO.contarLivrosOferecidos();
        if (oferecidos == 0) {
            return 0; // evita divisão por zero
        }
        return (int) Math.round(100.0 * livrosReutilizados() / oferecidos);
    }

    // Engajamento = (❤ + 💬) / posts. Ex: 1,5 = cada post recebe em média 1,5 interações
    public double taxaEngajamento() throws SQLException {
        int posts = indicadorDAO.contarExperiencias();
        if (posts == 0) {
            return 0;
        }
        int interacoes = indicadorDAO.contarGostei() + indicadorDAO.contarComentarios();
        return Math.round(10.0 * interacoes / posts) / 10.0; // 1 casa decimal
    }

    public List<Contagem> trocasPorMes() throws SQLException {
        return indicadorDAO.trocasPorMes();
    }

    public List<Contagem> rankingComunidade(int limite) throws SQLException {
        return indicadorDAO.rankingTrocas(limite);
    }

    public List<Contagem> generosMaisLidos() throws SQLException {
        return indicadorDAO.generosMaisLidos();
    }

    public List<Contagem> emAlta(int limite) throws SQLException {
        return indicadorDAO.livrosMaisCurtidos(limite);
    }

    public ImpactoUsuario impactoDoUsuario(int usuarioId) throws SQLException {
        int lidos = estanteDAO.contarPorStatus(usuarioId).get(StatusLeitura.LIDO);
        int favoritos = estanteDAO.listarFavoritos(usuarioId).size();
        int trocas = indicadorDAO.trocasDoUsuario(usuarioId);
        return new ImpactoUsuario(lidos, favoritos, trocas);
    }
}
