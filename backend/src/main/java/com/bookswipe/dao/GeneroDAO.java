package com.bookswipe.dao;

import com.bookswipe.model.Genero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD da tabela "genero".
 */
public class GeneroDAO {

    // CREATE
    public void inserir(Genero genero) throws SQLException {
        String sql = "INSERT INTO genero (nome) VALUES (?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, genero.getNome());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    genero.setId(chaves.getInt(1));
                }
            }
        }
    }

    // READ - todos
    public List<Genero> listarTodos() throws SQLException {
        String sql = "SELECT * FROM genero ORDER BY nome";
        List<Genero> generos = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                generos.add(new Genero(resultado.getInt("id"), resultado.getString("nome")));
            }
        }
        return generos;
    }

    // READ - um pelo id (retorna null se não achar)
    public Genero buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM genero WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next()
                        ? new Genero(resultado.getInt("id"), resultado.getString("nome"))
                        : null;
            }
        }
    }

    // UPDATE
    public void atualizar(Genero genero) throws SQLException {
        String sql = "UPDATE genero SET nome = ? WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, genero.getNome());
            comando.setInt(2, genero.getId());
            comando.executeUpdate();
        }
    }

    // DELETE
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM genero WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }
}
