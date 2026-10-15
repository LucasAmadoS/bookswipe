package com.bookswipe.dao;

import com.bookswipe.model.CondicaoLivro;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;
import com.bookswipe.model.LivroDisponivel;
import com.bookswipe.model.StatusDisponivel;
import com.bookswipe.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Exemplares oferecidos para troca (tabela "livro_disponivel") - o marketplace.
 */
public class LivroDisponivelDAO {

    // Junta 4 tabelas: o exemplar, o dono, a obra e o gênero da obra
    private static final String SELECT_DISPONIVEL =
            "SELECT ld.*, "
          + "       u.nome AS dono_nome, u.bairro AS dono_bairro, "
          + "       l.titulo, l.autor, l.capa_url, l.genero_id, g.nome AS genero_nome "
          + "FROM livro_disponivel ld "
          + "JOIN usuario u ON u.id = ld.usuario_id "
          + "JOIN livro   l ON l.id = ld.livro_id "
          + "LEFT JOIN genero g ON g.id = l.genero_id ";

    // CREATE - oferecer um livro
    public void inserir(LivroDisponivel ld) throws SQLException {
        String sql = "INSERT INTO livro_disponivel (usuario_id, livro_id, condicao, status) VALUES (?, ?, ?, ?)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setInt(1, ld.getDono().getId());
            comando.setInt(2, ld.getLivro().getId());
            comando.setString(3, ld.getCondicao().name());
            comando.setString(4, ld.getStatus().name());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    ld.setId(chaves.getInt(1));
                }
            }
        }
    }

    // READ - um pelo id (retorna null se não achar)
    public LivroDisponivel buscarPorId(int id) throws SQLException {
        List<LivroDisponivel> lista = buscar(SELECT_DISPONIVEL + "WHERE ld.id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Tela "Livros para troca": só DISPONIVEL e sem os livros do próprio usuário.
    // Filtro de gênero opcional (generoId = 0 traz todos).
    public List<LivroDisponivel> listarMarketplace(int usuarioId, int generoId) throws SQLException {
        String sql = SELECT_DISPONIVEL
                   + "WHERE ld.status = 'DISPONIVEL' AND ld.usuario_id <> ? "
                   + (generoId > 0 ? "AND l.genero_id = ? " : "")
                   + "ORDER BY l.titulo";
        return generoId > 0 ? buscar(sql, usuarioId, generoId) : buscar(sql, usuarioId);
    }

    // Aba "Disponíveis para troca" da estante: os exemplares que o usuário ofereceu
    public List<LivroDisponivel> listarDoUsuario(int usuarioId) throws SQLException {
        return buscar(SELECT_DISPONIVEL + "WHERE ld.usuario_id = ? ORDER BY l.titulo", usuarioId);
    }

    // UPDATE - o dono pode corrigir a condição
    public void atualizarCondicao(int id, CondicaoLivro condicao) throws SQLException {
        String sql = "UPDATE livro_disponivel SET condicao = ? WHERE id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, condicao.name());
            comando.setInt(2, id);
            comando.executeUpdate();
        }
    }

    // DELETE - tira do marketplace (e apaga os pedidos dele, na mesma transação)
    public void excluir(int id) throws SQLException {
        try (Connection conexao = ConexaoBanco.conectar()) {
            conexao.setAutoCommit(false);

            try (PreparedStatement pedidos = conexao.prepareStatement(
                         "DELETE FROM solicitacao_troca WHERE livro_disponivel_id = ?");
                 PreparedStatement exemplar = conexao.prepareStatement(
                         "DELETE FROM livro_disponivel WHERE id = ?")) {

                pedidos.setInt(1, id);
                pedidos.executeUpdate();
                exemplar.setInt(1, id);
                exemplar.executeUpdate();
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private List<LivroDisponivel> buscar(String sql, int... parametros) throws SQLException {
        List<LivroDisponivel> lista = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                comando.setInt(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(montar(resultado));
                }
            }
        }
        return lista;
    }

    // Também usado pelo SolicitacaoTrocaDAO, que faz o mesmo JOIN
    static LivroDisponivel montar(ResultSet resultado) throws SQLException {
        Usuario dono = new Usuario();
        dono.setId(resultado.getInt("usuario_id"));
        dono.setNome(resultado.getString("dono_nome"));
        dono.setBairro(resultado.getString("dono_bairro"));

        Livro livro = new Livro();
        livro.setId(resultado.getInt("livro_id"));
        livro.setTitulo(resultado.getString("titulo"));
        livro.setAutor(resultado.getString("autor"));
        livro.setCapaUrl(resultado.getString("capa_url"));
        int generoId = resultado.getInt("genero_id");
        if (!resultado.wasNull()) {
            livro.setGenero(new Genero(generoId, resultado.getString("genero_nome")));
        }

        LivroDisponivel ld = new LivroDisponivel();
        ld.setId(resultado.getInt("id"));
        ld.setDono(dono);
        ld.setLivro(livro);
        ld.setCondicao(CondicaoLivro.valueOf(resultado.getString("condicao")));
        ld.setStatus(StatusDisponivel.valueOf(resultado.getString("status")));
        return ld;
    }
}
