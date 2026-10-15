package com.bookswipe.dao;

import com.bookswipe.model.LivroDisponivel;
import com.bookswipe.model.SolicitacaoTroca;
import com.bookswipe.model.StatusSolicitacao;
import com.bookswipe.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedidos de troca (tabela "solicitacao_troca") e o fluxo:
 * PENDENTE -> ACEITA -> CONCLUIDA, ou PENDENTE -> RECUSADA.
 */
public class SolicitacaoTrocaDAO {

    // As colunas do pedido ganham apelidos (sol_...) para não misturar com id/status do exemplar
    private static final String SELECT_SOLICITACAO =
            "SELECT st.id AS sol_id, st.status AS sol_status, st.data_solicitacao, st.data_conclusao, "
          + "       st.solicitante_id, s.nome AS solicitante_nome, "
          + "       ld.*, u.nome AS dono_nome, u.bairro AS dono_bairro, "
          + "       l.titulo, l.autor, l.capa_url, l.genero_id, g.nome AS genero_nome "
          + "FROM solicitacao_troca st "
          + "JOIN usuario s           ON s.id  = st.solicitante_id "
          + "JOIN livro_disponivel ld ON ld.id = st.livro_disponivel_id "
          + "JOIN usuario u           ON u.id  = ld.usuario_id "
          + "JOIN livro l             ON l.id  = ld.livro_id "
          + "LEFT JOIN genero g       ON g.id  = l.genero_id ";

    // CREATE - "Solicitar troca"
    public void inserir(SolicitacaoTroca sol) throws SQLException {
        String sql = "INSERT INTO solicitacao_troca (livro_disponivel_id, solicitante_id, status) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setInt(1, sol.getLivroDisponivel().getId());
            comando.setInt(2, sol.getSolicitante().getId());
            comando.setString(3, sol.getStatus().name());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    sol.setId(chaves.getInt(1));
                }
            }
        }
    }

    // READ - um pedido (retorna null se não achar)
    public SolicitacaoTroca buscarPorId(int id) throws SQLException {
        List<SolicitacaoTroca> lista = buscar(SELECT_SOLICITACAO + "WHERE st.id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Pedidos que chegaram nos MEUS livros (para aceitar ou recusar)
    public List<SolicitacaoTroca> listarRecebidas(int donoId) throws SQLException {
        return buscar(SELECT_SOLICITACAO + "WHERE ld.usuario_id = ? ORDER BY st.data_solicitacao DESC", donoId);
    }

    // Pedidos que EU fiz (para acompanhar o status)
    public List<SolicitacaoTroca> listarFeitas(int solicitanteId) throws SQLException {
        return buscar(SELECT_SOLICITACAO + "WHERE st.solicitante_id = ? ORDER BY st.data_solicitacao DESC",
                solicitanteId);
    }

    // O dono aceita: pedido ACEITA, livro RESERVADO, outros pedidos do mesmo livro RECUSADA.
    // Retorna false se o pedido não estava PENDENTE (nada muda).
    public boolean aceitar(int id) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            conexao.setAutoCommit(false);
            try {
                if (mudarStatus(conexao, id, StatusSolicitacao.PENDENTE, StatusSolicitacao.ACEITA) == 0) {
                    conexao.rollback();
                    return false;
                }
                int livroDisponivelId = buscarLivroDisponivelId(conexao, id);

                executar(conexao, "UPDATE livro_disponivel SET status = 'RESERVADO' WHERE id = ?",
                        livroDisponivelId);
                executar(conexao, "UPDATE solicitacao_troca SET status = 'RECUSADA' "
                        + "WHERE livro_disponivel_id = ? AND status = 'PENDENTE'", livroDisponivelId);

                conexao.commit();
                return true;
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    // O dono recusa. Retorna false se o pedido não estava PENDENTE.
    public boolean recusar(int id) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            return mudarStatus(conexao, id, StatusSolicitacao.PENDENTE, StatusSolicitacao.RECUSADA) > 0;
        }
    }

    // A troca aconteceu: pedido CONCLUIDA (com data) e livro TROCADO. +1 livro reutilizado ♻️
    // Retorna false se o pedido não estava ACEITA.
    public boolean concluir(int id) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            conexao.setAutoCommit(false);
            try {
                String sql = "UPDATE solicitacao_troca SET status = 'CONCLUIDA', data_conclusao = NOW() "
                           + "WHERE id = ? AND status = 'ACEITA'";
                if (executar(conexao, sql, id) == 0) {
                    conexao.rollback();
                    return false;
                }
                executar(conexao, "UPDATE livro_disponivel SET status = 'TROCADO' WHERE id = ?",
                        buscarLivroDisponivelId(conexao, id));

                conexao.commit();
                return true;
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    // Indicador principal de sustentabilidade: quantos livros voltaram a circular
    public int contarConcluidas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM solicitacao_troca WHERE status = 'CONCLUIDA'";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    // DELETE - cancelar um pedido
    public void excluir(int id) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            executar(conexao, "DELETE FROM solicitacao_troca WHERE id = ?", id);
        }
    }

    // UPDATE ... WHERE status = de: só muda se estiver no status esperado. Retorna quantas linhas mudaram.
    private int mudarStatus(Connection conexao, int id, StatusSolicitacao de, StatusSolicitacao para)
            throws SQLException {
        String sql = "UPDATE solicitacao_troca SET status = ? WHERE id = ? AND status = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, para.name());
            comando.setInt(2, id);
            comando.setString(3, de.name());
            return comando.executeUpdate();
        }
    }

    private int buscarLivroDisponivelId(Connection conexao, int solicitacaoId) throws SQLException {
        String sql = "SELECT livro_disponivel_id FROM solicitacao_troca WHERE id = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, solicitacaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    private int executar(Connection conexao, String sql, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            return comando.executeUpdate();
        }
    }

    private List<SolicitacaoTroca> buscar(String sql, int parametro) throws SQLException {
        List<SolicitacaoTroca> lista = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, parametro);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(montar(resultado));
                }
            }
        }
        return lista;
    }

    private SolicitacaoTroca montar(ResultSet resultado) throws SQLException {
        LivroDisponivel ld = LivroDisponivelDAO.montar(resultado); // reaproveita a montagem do exemplar

        Usuario solicitante = new Usuario();
        solicitante.setId(resultado.getInt("solicitante_id"));
        solicitante.setNome(resultado.getString("solicitante_nome"));

        SolicitacaoTroca sol = new SolicitacaoTroca();
        sol.setId(resultado.getInt("sol_id"));
        sol.setLivroDisponivel(ld);
        sol.setSolicitante(solicitante);
        sol.setStatus(StatusSolicitacao.valueOf(resultado.getString("sol_status")));

        Timestamp pedido = resultado.getTimestamp("data_solicitacao");
        if (pedido != null) {
            sol.setDataSolicitacao(pedido.toLocalDateTime());
        }
        Timestamp conclusao = resultado.getTimestamp("data_conclusao");
        if (conclusao != null) {
            sol.setDataConclusao(conclusao.toLocalDateTime());
        }
        return sol;
    }
}
