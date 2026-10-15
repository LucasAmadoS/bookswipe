package com.bookswipe.dao;

import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD da tabela "livro".
 * As buscas usam JOIN com "genero" para já trazer o nome do gênero junto.
 */
public class LivroDAO {

    // Parte do SELECT que todas as buscas usam.
    // LEFT JOIN: traz o livro mesmo se ele não tiver gênero.
    private static final String SELECT_LIVRO =
            "SELECT l.*, g.nome AS genero_nome "
          + "FROM livro l LEFT JOIN genero g ON g.id = l.genero_id ";

    // CREATE
    public void inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (titulo, autor, isbn, sinopse, capa_url, genero_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherCampos(comando, livro);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    livro.setId(chaves.getInt(1));
                }
            }
        }
    }

    // READ - todos
    public List<Livro> listarTodos() throws SQLException {
        String sql = SELECT_LIVRO + "ORDER BY l.titulo";
        List<Livro> livros = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                livros.add(montarLivro(resultado));
            }
        }
        return livros;
    }

    // READ - um pelo id (retorna null se não achar)
    public Livro buscarPorId(int id) throws SQLException {
        String sql = SELECT_LIVRO + "WHERE l.id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarLivro(resultado) : null;
            }
        }
    }

    // READ - pesquisa por parte do título ou do autor ("hob" acha "O Hobbit")
    public List<Livro> pesquisar(String texto) throws SQLException {
        String sql = SELECT_LIVRO + "WHERE l.titulo LIKE ? OR l.autor LIKE ? ORDER BY l.titulo";
        List<Livro> livros = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, "%" + texto + "%");
            comando.setString(2, "%" + texto + "%");
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    livros.add(montarLivro(resultado));
                }
            }
        }
        return livros;
    }

    // Livros que interessam a um usuário (base do match):
    // os que ele já leu + os livros dos posts em que ele deu ❤.
    // UNION junta os dois SELECTs e tira os repetidos.
    public List<Livro> listarDeInteresse(int usuarioId) throws SQLException {
        String sql = SELECT_LIVRO
                   + "WHERE l.id IN ("
                   + "    SELECT livro_id FROM estante WHERE usuario_id = ? AND status = 'LIDO' "
                   + "    UNION "
                   + "    SELECT e.livro_id FROM curtida c JOIN experiencia e ON e.id = c.experiencia_id "
                   + "     WHERE c.usuario_id = ? AND c.tipo = 'GOSTEI'"
                   + ") ORDER BY l.titulo";
        List<Livro> livros = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, usuarioId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    livros.add(montarLivro(resultado));
                }
            }
        }
        return livros;
    }

    // UPDATE
    public void atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livro SET titulo = ?, autor = ?, isbn = ?, sinopse = ?, capa_url = ?, genero_id = ? "
                   + "WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            preencherCampos(comando, livro);
            comando.setInt(7, livro.getId());
            comando.executeUpdate();
        }
    }

    // DELETE
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM livro WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    // Preenche os 6 "?" que o INSERT e o UPDATE têm em comum
    private void preencherCampos(PreparedStatement comando, Livro livro) throws SQLException {
        comando.setString(1, livro.getTitulo());
        comando.setString(2, livro.getAutor());
        comando.setString(3, livro.getIsbn());
        comando.setString(4, livro.getSinopse());
        comando.setString(5, livro.getCapaUrl());
        if (livro.getGenero() != null) {
            comando.setInt(6, livro.getGenero().getId());
        } else {
            comando.setNull(6, Types.INTEGER);
        }
    }

    // Transforma a linha atual do ResultSet em um objeto Livro (com o Genero dentro)
    private Livro montarLivro(ResultSet resultado) throws SQLException {
        Livro livro = new Livro();
        livro.setId(resultado.getInt("id"));
        livro.setTitulo(resultado.getString("titulo"));
        livro.setAutor(resultado.getString("autor"));
        livro.setIsbn(resultado.getString("isbn"));
        livro.setSinopse(resultado.getString("sinopse"));
        livro.setCapaUrl(resultado.getString("capa_url"));

        int generoId = resultado.getInt("genero_id");
        if (!resultado.wasNull()) {
            livro.setGenero(new Genero(generoId, resultado.getString("genero_nome")));
        }
        return livro;
    }
}
