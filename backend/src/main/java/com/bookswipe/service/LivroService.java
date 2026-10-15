package com.bookswipe.service;

import com.bookswipe.dao.EstanteDAO;
import com.bookswipe.dao.GeneroDAO;
import com.bookswipe.dao.LivroDAO;
import com.bookswipe.model.Genero;
import com.bookswipe.model.ItemEstante;
import com.bookswipe.model.Livro;
import com.bookswipe.model.StatusLeitura;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Catálogo de livros e "Minha estante".
 */
public class LivroService {

    private final LivroDAO livroDAO = new LivroDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final EstanteDAO estanteDAO = new EstanteDAO();

    public List<Livro> pesquisar(String texto) throws SQLException {
        return texto.isBlank() ? livroDAO.listarTodos() : livroDAO.pesquisar(texto);
    }

    public Livro buscar(int livroId) throws SQLException {
        Livro livro = livroDAO.buscarPorId(livroId);
        if (livro == null) {
            throw new RegraNegocioException("Livro " + livroId + " não encontrado");
        }
        return livro;
    }

    public Livro cadastrar(String titulo, String autor, int generoId) throws SQLException {
        if (titulo.isBlank() || autor.isBlank()) {
            throw new RegraNegocioException("Título e autor são obrigatórios");
        }
        Genero genero = generoDAO.buscarPorId(generoId);
        if (genero == null) {
            throw new RegraNegocioException("Gênero " + generoId + " não existe");
        }
        Livro livro = new Livro(titulo, autor, genero);
        livroDAO.inserir(livro);
        return livro;
    }

    public List<Genero> generos() throws SQLException {
        return generoDAO.listarTodos();
    }

    // ---------- Minha estante ----------

    public List<ItemEstante> estanteInteira(int usuarioId) throws SQLException {
        return estanteDAO.listar(usuarioId);
    }

    public List<ItemEstante> estante(int usuarioId, StatusLeitura status) throws SQLException {
        return estanteDAO.listarPorStatus(usuarioId, status);
    }

    public List<ItemEstante> favoritos(int usuarioId) throws SQLException {
        return estanteDAO.listarFavoritos(usuarioId);
    }

    public Map<StatusLeitura, Integer> totaisDaEstante(int usuarioId) throws SQLException {
        return estanteDAO.contarPorStatus(usuarioId);
    }

    // Muda o status mantendo o favorito
    public void colocarNaEstante(int usuarioId, int livroId, StatusLeitura status) throws SQLException {
        ItemEstante atual = estanteDAO.buscar(usuarioId, livroId);
        boolean favorito = atual != null && atual.isFavorito();
        estanteDAO.salvar(new ItemEstante(usuarioId, buscar(livroId), status, favorito));
    }

    // Liga/desliga o favorito. Livro que não está na estante entra como QUERO_LER.
    public boolean alternarFavorito(int usuarioId, int livroId) throws SQLException {
        ItemEstante atual = estanteDAO.buscar(usuarioId, livroId);
        StatusLeitura status = (atual != null) ? atual.getStatus() : StatusLeitura.QUERO_LER;
        boolean novoFavorito = atual == null || !atual.isFavorito();
        estanteDAO.salvar(new ItemEstante(usuarioId, buscar(livroId), status, novoFavorito));
        return novoFavorito;
    }

    public void removerDaEstante(int usuarioId, int livroId) throws SQLException {
        estanteDAO.remover(usuarioId, livroId);
    }
}
