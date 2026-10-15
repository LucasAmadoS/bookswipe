package com.bookswipe.dao;

import com.bookswipe.model.Comentario;
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
 * CRUD da tabela "comentario".
 */
public class ComentarioDAO {

    // CREATE
    public void inserir(Comentario comentario) throws SQLException {
        String sql = "INSERT INTO comentario (usuario_id, experiencia_id, texto) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setInt(1, comentario.getUsuario().getId());
            comando.setInt(2, comentario.getExperienciaId());
            comando.setString(3, comentario.getTexto());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    comentario.setId(chaves.getInt(1));
                }
            }
        }
    }

    // READ - comentários de um post, do mais antigo para o mais novo (ordem de conversa)
    public List<Comentario> listarPorExperiencia(int experienciaId) throws SQLException {
        String sql = "SELECT c.*, u.nome AS usuario_nome "
                   + "FROM comentario c "
                   + "JOIN usuario u ON u.id = c.usuario_id "
                   + "WHERE c.experiencia_id = ? "
                   + "ORDER BY c.data_comentario";
        List<Comentario> comentarios = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, experienciaId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    comentarios.add(montarComentario(resultado));
                }
            }
        }
        return comentarios;
    }

    // UPDATE - só o texto pode ser editado
    public void atualizar(Comentario comentario) throws SQLException {
        String sql = "UPDATE comentario SET texto = ? WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, comentario.getTexto());
            comando.setInt(2, comentario.getId());
            comando.executeUpdate();
        }
    }

    // DELETE
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM comentario WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    private Comentario montarComentario(ResultSet resultado) throws SQLException {
        Usuario autor = new Usuario();
        autor.setId(resultado.getInt("usuario_id"));
        autor.setNome(resultado.getString("usuario_nome"));

        Comentario comentario = new Comentario();
        comentario.setId(resultado.getInt("id"));
        comentario.setUsuario(autor);
        comentario.setExperienciaId(resultado.getInt("experiencia_id"));
        comentario.setTexto(resultado.getString("texto"));

        Timestamp data = resultado.getTimestamp("data_comentario");
        if (data != null) {
            comentario.setDataComentario(data.toLocalDateTime());
        }
        return comentario;
    }
}
