package com.bookswipe.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Tabela "sessao": quem está logado. Guarda o hash do token, nunca o token.
 */
public class SessaoDAO {

    public void criar(String tokenHash, int usuarioId, LocalDateTime expiraEm) throws SQLException {
        String sql = "INSERT INTO sessao (token_hash, usuario_id, expira_em) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, tokenHash);
            comando.setInt(2, usuarioId);
            comando.setTimestamp(3, Timestamp.valueOf(expiraEm));
            comando.executeUpdate();
        }
    }

    // Dono do token, se ele existir E ainda não tiver vencido. 0 = sessão inválida.
    public int buscarUsuarioId(String tokenHash) throws SQLException {
        String sql = "SELECT usuario_id FROM sessao WHERE token_hash = ? AND expira_em > NOW()";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, tokenHash);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? resultado.getInt("usuario_id") : 0;
            }
        }
    }

    // Logout
    public void excluir(String tokenHash) throws SQLException {
        executar("DELETE FROM sessao WHERE token_hash = ?", tokenHash);
    }

    // Faxina: apaga sessões vencidas (roda a cada login)
    public void excluirVencidas() throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement("DELETE FROM sessao WHERE expira_em <= NOW()")) {
            comando.executeUpdate();
        }
    }

    private void executar(String sql, String valor) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, valor);
            comando.executeUpdate();
        }
    }
}
