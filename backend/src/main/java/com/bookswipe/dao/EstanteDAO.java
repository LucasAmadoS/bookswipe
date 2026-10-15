package com.bookswipe.dao;

import com.bookswipe.model.ItemEstante;
import com.bookswipe.model.Livro;
import com.bookswipe.model.StatusLeitura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * "Minha estante": o que o usuário quer ler, está lendo, já leu e seus favoritos.
 */
public class EstanteDAO {

    private static final String SELECT_ESTANTE =
            "SELECT e.*, l.titulo, l.autor, l.capa_url "
          + "FROM estante e JOIN livro l ON l.id = e.livro_id ";

    // CREATE ou UPDATE: coloca o livro na estante, ou muda o status se ele já estiver lá
    public void salvar(ItemEstante item) throws SQLException {
        String sql = "INSERT INTO estante (usuario_id, livro_id, status, favorito) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE status = VALUES(status), favorito = VALUES(favorito)";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, item.getUsuarioId());
            comando.setInt(2, item.getLivro().getId());
            comando.setString(3, item.getStatus().name());
            comando.setBoolean(4, item.isFavorito());
            comando.executeUpdate();
        }
    }

    // READ - um livro da estante (null = não está na estante)
    public ItemEstante buscar(int usuarioId, int livroId) throws SQLException {
        String sql = SELECT_ESTANTE + "WHERE e.usuario_id = ? AND e.livro_id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, livroId);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarItem(resultado) : null;
            }
        }
    }

    // READ - a estante inteira
    public List<ItemEstante> listar(int usuarioId) throws SQLException {
        String sql = SELECT_ESTANTE + "WHERE e.usuario_id = ? ORDER BY l.titulo";
        return buscar(sql, usuarioId, null);
    }

    // READ - uma aba: Quero ler / Lendo / Lidos
    public List<ItemEstante> listarPorStatus(int usuarioId, StatusLeitura status) throws SQLException {
        String sql = SELECT_ESTANTE + "WHERE e.usuario_id = ? AND e.status = ? ORDER BY l.titulo";
        return buscar(sql, usuarioId, status.name());
    }

    // READ - aba Favoritos
    public List<ItemEstante> listarFavoritos(int usuarioId) throws SQLException {
        String sql = SELECT_ESTANTE + "WHERE e.usuario_id = ? AND e.favorito = TRUE ORDER BY l.titulo";
        return buscar(sql, usuarioId, null);
    }

    // Números do perfil ("24 livros lidos"): conta quantos livros há em cada status.
    // GROUP BY junta as linhas com o mesmo status e o COUNT conta cada grupo.
    public Map<StatusLeitura, Integer> contarPorStatus(int usuarioId) throws SQLException {
        String sql = "SELECT status, COUNT(*) AS total FROM estante WHERE usuario_id = ? GROUP BY status";

        Map<StatusLeitura, Integer> totais = new EnumMap<>(StatusLeitura.class);
        for (StatusLeitura s : StatusLeitura.values()) {
            totais.put(s, 0); // status sem nenhum livro aparece com 0
        }

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    totais.put(StatusLeitura.valueOf(resultado.getString("status")), resultado.getInt("total"));
                }
            }
        }
        return totais;
    }

    // DELETE - tira o livro da estante
    public void remover(int usuarioId, int livroId) throws SQLException {
        String sql = "DELETE FROM estante WHERE usuario_id = ? AND livro_id = ?";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, livroId);
            comando.executeUpdate();
        }
    }

    // Roda o SELECT com o id do usuário e, se tiver, o status
    private List<ItemEstante> buscar(String sql, int usuarioId, String status) throws SQLException {
        List<ItemEstante> itens = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);
            if (status != null) {
                comando.setString(2, status);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    itens.add(montarItem(resultado));
                }
            }
        }
        return itens;
    }

    private ItemEstante montarItem(ResultSet resultado) throws SQLException {
        Livro livro = new Livro();
        livro.setId(resultado.getInt("livro_id"));
        livro.setTitulo(resultado.getString("titulo"));
        livro.setAutor(resultado.getString("autor"));
        livro.setCapaUrl(resultado.getString("capa_url"));

        return new ItemEstante(
                resultado.getInt("usuario_id"),
                livro,
                StatusLeitura.valueOf(resultado.getString("status")),
                resultado.getBoolean("favorito"));
    }
}
