package com.bookswipe.dao;

import com.bookswipe.model.Experiencia;
import com.bookswipe.model.Livro;
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
 * CRUD da tabela "experiencia" + as buscas das telas (Descobrir, página do livro, perfil).
 */
public class ExperienciaDAO {

    // SELECT base: junta usuário e livro e conta curtidas/comentários com subconsultas
    private static final String SELECT_EXPERIENCIA =
            "SELECT e.*, "
          + "       u.nome   AS usuario_nome, "
          + "       l.titulo AS livro_titulo, "
          + "       l.autor  AS livro_autor, "
          + "       l.capa_url AS livro_capa, "
          + "       (SELECT COUNT(*) FROM curtida c "
          + "         WHERE c.experiencia_id = e.id AND c.tipo = 'GOSTEI') AS total_gostei, "
          + "       (SELECT COUNT(*) FROM comentario co "
          + "         WHERE co.experiencia_id = e.id) AS total_comentarios "
          + "FROM experiencia e "
          + "JOIN usuario u ON u.id = e.usuario_id "
          + "JOIN livro   l ON l.id = e.livro_id ";

    // CREATE
    public void inserir(Experiencia exp) throws SQLException {
        String sql = "INSERT INTO experiencia (usuario_id, livro_id, nota, sentimento, texto, trecho_favorito) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setInt(1, exp.getUsuario().getId());
            comando.setInt(2, exp.getLivro().getId());
            comando.setInt(3, exp.getNota());
            comando.setString(4, exp.getSentimento());
            comando.setString(5, exp.getTexto());
            comando.setString(6, exp.getTrechoFavorito());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    exp.setId(chaves.getInt(1));
                }
            }
        }
    }

    // READ - uma pelo id (retorna null se não achar)
    public Experiencia buscarPorId(int id) throws SQLException {
        List<Experiencia> lista = buscar(SELECT_EXPERIENCIA + "WHERE e.id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Tela "Descobrir": posts de OUTRAS pessoas que o usuário ainda não avaliou (❤ ou ❌)
    public List<Experiencia> listarParaDescobrir(int usuarioId) throws SQLException {
        String sql = SELECT_EXPERIENCIA
                   + "WHERE e.usuario_id <> ? "
                   + "  AND NOT EXISTS (SELECT 1 FROM curtida c "
                   + "                   WHERE c.experiencia_id = e.id AND c.usuario_id = ?) "
                   + "ORDER BY e.data_post DESC";
        return buscar(sql, usuarioId, usuarioId);
    }

    // Página do livro: "Experiências dos leitores"
    public List<Experiencia> listarPorLivro(int livroId) throws SQLException {
        return buscar(SELECT_EXPERIENCIA + "WHERE e.livro_id = ? ORDER BY e.data_post DESC", livroId);
    }

    // Perfil: "Minhas experiências"
    public List<Experiencia> listarPorUsuario(int usuarioId) throws SQLException {
        return buscar(SELECT_EXPERIENCIA + "WHERE e.usuario_id = ? ORDER BY e.data_post DESC", usuarioId);
    }

    // UPDATE - o autor pode editar nota, sentimento e textos
    public void atualizar(Experiencia exp) throws SQLException {
        String sql = "UPDATE experiencia SET nota = ?, sentimento = ?, texto = ?, trecho_favorito = ? "
                   + "WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, exp.getNota());
            comando.setString(2, exp.getSentimento());
            comando.setString(3, exp.getTexto());
            comando.setString(4, exp.getTrechoFavorito());
            comando.setInt(5, exp.getId());
            comando.executeUpdate();
        }
    }

    // DELETE - precisa apagar antes as curtidas e comentários (por causa das FKs).
    // Usa TRANSAÇÃO: ou apaga as 3 coisas, ou não apaga nada.
    public void excluir(int id) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            conexao.setAutoCommit(false); // começa a transação

            try {
                executar(conexao, "DELETE FROM comentario WHERE experiencia_id = ?", id);
                executar(conexao, "DELETE FROM curtida WHERE experiencia_id = ?", id);
                executar(conexao, "DELETE FROM experiencia WHERE id = ?", id);
                conexao.commit();   // deu tudo certo: confirma
            } catch (SQLException e) {
                conexao.rollback(); // deu erro no meio: desfaz tudo
                throw e;
            }
        }
    }

    private void executar(Connection conexao, String sql, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    // Roda um SELECT com parâmetros inteiros e monta a lista
    private List<Experiencia> buscar(String sql, int... parametros) throws SQLException {
        List<Experiencia> lista = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                comando.setInt(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(montarExperiencia(resultado));
                }
            }
        }
        return lista;
    }

    private Experiencia montarExperiencia(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(resultado.getInt("usuario_id"));
        usuario.setNome(resultado.getString("usuario_nome"));

        Livro livro = new Livro();
        livro.setId(resultado.getInt("livro_id"));
        livro.setTitulo(resultado.getString("livro_titulo"));
        livro.setAutor(resultado.getString("livro_autor"));
        livro.setCapaUrl(resultado.getString("livro_capa"));

        Experiencia exp = new Experiencia();
        exp.setId(resultado.getInt("id"));
        exp.setUsuario(usuario);
        exp.setLivro(livro);
        exp.setNota(resultado.getInt("nota"));
        exp.setSentimento(resultado.getString("sentimento"));
        exp.setTexto(resultado.getString("texto"));
        exp.setTrechoFavorito(resultado.getString("trecho_favorito"));
        exp.setTotalGostei(resultado.getInt("total_gostei"));
        exp.setTotalComentarios(resultado.getInt("total_comentarios"));

        Timestamp data = resultado.getTimestamp("data_post");
        if (data != null) {
            exp.setDataPost(data.toLocalDateTime());
        }
        return exp;
    }
}
