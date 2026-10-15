package com.bookswipe.dao;

import com.bookswipe.model.Contagem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de agrupamento para os indicadores (telas "Meu impacto" e dashboard).
 * Só leitura: nenhum método aqui altera o banco.
 */
public class IndicadorDAO {

    // Gráfico "Livros reutilizados" por mês. DATE_FORMAT transforma a data em "2026-06".
    public List<Contagem> trocasPorMes() throws SQLException {
        String sql = "SELECT DATE_FORMAT(data_conclusao, '%Y-%m') AS rotulo, COUNT(*) AS total "
                   + "FROM solicitacao_troca WHERE status = 'CONCLUIDA' "
                   + "GROUP BY rotulo ORDER BY rotulo";
        return listarContagens(sql);
    }

    // Ranking da comunidade: quem mais participou de trocas concluídas (doando OU recebendo).
    // UNION ALL junta as duas listas sem tirar repetidos, para contar cada participação.
    public List<Contagem> rankingTrocas(int limite) throws SQLException {
        String sql = "SELECT u.nome AS rotulo, COUNT(*) AS total "
                   + "FROM ("
                   + "    SELECT ld.usuario_id AS uid FROM solicitacao_troca st "
                   + "      JOIN livro_disponivel ld ON ld.id = st.livro_disponivel_id "
                   + "     WHERE st.status = 'CONCLUIDA' "
                   + "    UNION ALL "
                   + "    SELECT st.solicitante_id FROM solicitacao_troca st WHERE st.status = 'CONCLUIDA'"
                   + ") participacoes "
                   + "JOIN usuario u ON u.id = participacoes.uid "
                   + "GROUP BY u.id, u.nome ORDER BY total DESC, u.nome LIMIT ?";
        return listarContagens(sql, limite);
    }

    // Gêneros com mais experiências publicadas
    public List<Contagem> generosMaisLidos() throws SQLException {
        String sql = "SELECT g.nome AS rotulo, COUNT(*) AS total "
                   + "FROM experiencia e "
                   + "JOIN livro l  ON l.id = e.livro_id "
                   + "JOIN genero g ON g.id = l.genero_id "
                   + "GROUP BY g.id, g.nome ORDER BY total DESC, g.nome";
        return listarContagens(sql);
    }

    // "Em alta": livros cujos posts receberam mais ❤
    public List<Contagem> livrosMaisCurtidos(int limite) throws SQLException {
        String sql = "SELECT l.titulo AS rotulo, COUNT(*) AS total "
                   + "FROM curtida c "
                   + "JOIN experiencia e ON e.id = c.experiencia_id "
                   + "JOIN livro l       ON l.id = e.livro_id "
                   + "WHERE c.tipo = 'GOSTEI' "
                   + "GROUP BY l.id, l.titulo ORDER BY total DESC, l.titulo LIMIT ?";
        return listarContagens(sql, limite);
    }

    // Trocas concluídas em que o usuário participou (doou ou recebeu)
    public int trocasDoUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM solicitacao_troca st "
                   + "JOIN livro_disponivel ld ON ld.id = st.livro_disponivel_id "
                   + "WHERE st.status = 'CONCLUIDA' AND (ld.usuario_id = ? OR st.solicitante_id = ?)";
        return contar(sql, usuarioId, usuarioId);
    }

    // Totais simples, usados na taxa de reuso e na taxa de engajamento
    public int contarLivrosOferecidos() throws SQLException {
        return contar("SELECT COUNT(*) FROM livro_disponivel");
    }

    public int contarExperiencias() throws SQLException {
        return contar("SELECT COUNT(*) FROM experiencia");
    }

    public int contarGostei() throws SQLException {
        return contar("SELECT COUNT(*) FROM curtida WHERE tipo = 'GOSTEI'");
    }

    public int contarComentarios() throws SQLException {
        return contar("SELECT COUNT(*) FROM comentario");
    }

    private int contar(String sql, int... parametros) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                comando.setInt(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    private List<Contagem> listarContagens(String sql, int... parametros) throws SQLException {
        List<Contagem> lista = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                comando.setInt(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(new Contagem(resultado.getString("rotulo"), resultado.getInt("total")));
                }
            }
        }
        return lista;
    }
}
