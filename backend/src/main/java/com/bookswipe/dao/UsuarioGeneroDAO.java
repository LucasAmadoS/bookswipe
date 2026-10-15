package com.bookswipe.dao;

import com.bookswipe.model.Genero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gêneros favoritos de cada usuário (tabela N:N "usuario_genero").
 * É o que a tela "Crie seu perfil de leitor" salva, e a base do match.
 */
public class UsuarioGeneroDAO {

    // READ - os gêneros que o usuário marcou
    public List<Genero> listarDoUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT g.id, g.nome "
                   + "FROM usuario_genero ug JOIN genero g ON g.id = ug.genero_id "
                   + "WHERE ug.usuario_id = ? ORDER BY g.nome";
        List<Genero> generos = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    generos.add(new Genero(resultado.getInt("id"), resultado.getString("nome")));
                }
            }
        }
        return generos;
    }

    // Substitui a lista inteira de favoritos (o usuário marcou de novo os botões).
    // Transação: apaga os antigos e grava os novos. Se der erro, nada muda.
    // Batch: junta vários INSERTs e manda para o banco de uma vez só.
    public void definir(int usuarioId, List<Integer> generoIds) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            conexao.setAutoCommit(false);

            try (PreparedStatement apagar = conexao.prepareStatement(
                         "DELETE FROM usuario_genero WHERE usuario_id = ?");
                 PreparedStatement inserir = conexao.prepareStatement(
                         "INSERT INTO usuario_genero (usuario_id, genero_id) VALUES (?, ?)")) {

                apagar.setInt(1, usuarioId);
                apagar.executeUpdate();

                for (int generoId : generoIds) {
                    inserir.setInt(1, usuarioId);
                    inserir.setInt(2, generoId);
                    inserir.addBatch(); // guarda na fila
                }
                inserir.executeBatch(); // manda a fila toda

                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }
}
