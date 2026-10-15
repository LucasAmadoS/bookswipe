package com.bookswipe.dao;

import com.bookswipe.model.Curtida;
import com.bookswipe.model.TipoCurtida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * O swipe: grava ❤ GOSTEI ou ❌ PASSAR na tabela "curtida".
 */
public class CurtidaDAO {

    // CREATE ou UPDATE numa tacada só.
    // A tabela tem UNIQUE (usuario_id, experiencia_id): cada pessoa vota 1 vez por post.
    // Se já votou, o ON DUPLICATE KEY UPDATE só troca o tipo (mudou de ideia).
    public void registrar(Curtida curtida) throws SQLException {
        String sql = "INSERT INTO curtida (usuario_id, experiencia_id, tipo) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE tipo = VALUES(tipo), data_curtida = CURRENT_TIMESTAMP";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, curtida.getUsuarioId());
            comando.setInt(2, curtida.getExperienciaId());
            comando.setString(3, curtida.getTipo().name()); // enum -> "GOSTEI"
            comando.executeUpdate();
        }
    }

    // READ - o voto de um usuário em um post (null = ainda não votou)
    public Curtida buscar(int usuarioId, int experienciaId) throws SQLException {
        String sql = "SELECT * FROM curtida WHERE usuario_id = ? AND experiencia_id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, experienciaId);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarCurtida(resultado) : null;
            }
        }
    }

    // Quantos ❤ (ou ❌) um post recebeu
    public int contar(int experienciaId, TipoCurtida tipo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM curtida WHERE experiencia_id = ? AND tipo = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, experienciaId);
            comando.setString(2, tipo.name());
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    // DELETE - desfaz o voto (o post volta para a tela Descobrir)
    public void remover(int usuarioId, int experienciaId) throws SQLException {
        String sql = "DELETE FROM curtida WHERE usuario_id = ? AND experiencia_id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, experienciaId);
            comando.executeUpdate();
        }
    }

    private Curtida montarCurtida(ResultSet resultado) throws SQLException {
        Curtida curtida = new Curtida();
        curtida.setId(resultado.getInt("id"));
        curtida.setUsuarioId(resultado.getInt("usuario_id"));
        curtida.setExperienciaId(resultado.getInt("experiencia_id"));
        curtida.setTipo(TipoCurtida.valueOf(resultado.getString("tipo"))); // "GOSTEI" -> enum

        Timestamp data = resultado.getTimestamp("data_curtida");
        if (data != null) {
            curtida.setDataCurtida(data.toLocalDateTime());
        }
        return curtida;
    }
}
