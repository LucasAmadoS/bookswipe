package com.bookswipe.service;

import com.bookswipe.dao.GeneroDAO;
import com.bookswipe.dao.LivroDAO;
import com.bookswipe.externo.BuscadorDeLivros;
import com.bookswipe.externo.GoogleBooksBuscador;
import com.bookswipe.externo.OpenLibraryBuscador;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;
import com.bookswipe.model.SugestaoLivro;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Consumo de API pública de livros: buscar na internet, importar para o nosso banco
 * e completar capa/ISBN/sinopse dos livros que já temos.
 */
public class CatalogoExternoService {

    private final BuscadorDeLivros buscador = escolherBuscador();
    private final LivroDAO livroDAO = new LivroDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();

    // Com chave da Google configurada -> Google Books. Sem chave -> Open Library.
    private static BuscadorDeLivros escolherBuscador() {
        Properties config = new Properties();
        try (InputStream arquivo = CatalogoExternoService.class.getResourceAsStream("/apis.properties")) {
            if (arquivo != null) {
                config.load(arquivo);
            }
        } catch (IOException e) {
            // sem arquivo = sem chave
        }
        String chave = config.getProperty("google.books.chave", "").trim();
        return chave.isEmpty() ? new OpenLibraryBuscador() : new GoogleBooksBuscador(chave);
    }

    public String fonte() {
        return buscador.nome();
    }

    // Pesquisa na API de fora (não salva nada)
    public List<SugestaoLivro> buscar(String texto) {
        if (texto == null || texto.trim().length() < 2) {
            throw new RegraNegocioException("Digite pelo menos 2 letras para buscar");
        }
        try {
            return buscador.buscar(texto.trim());
        } catch (IOException e) {
            throw new RegraNegocioException("Não consegui consultar a " + fonte() + ": " + e.getMessage());
        }
    }

    // Salva no nosso banco um livro escolhido da busca
    public Livro importar(SugestaoLivro sugestao, int generoId) throws SQLException {
        if (sugestao.titulo() == null || sugestao.titulo().isBlank()
                || sugestao.autor() == null || sugestao.autor().isBlank()) {
            throw new RegraNegocioException("O livro precisa ter título e autor");
        }
        Genero genero = generoDAO.buscarPorId(generoId);
        if (genero == null) {
            throw new RegraNegocioException("Escolha um gênero válido");
        }
        boolean jaExiste = livroDAO.pesquisar(sugestao.titulo()).stream()
                .anyMatch(l -> l.getTitulo().equalsIgnoreCase(sugestao.titulo())
                        && l.getAutor().equalsIgnoreCase(sugestao.autor()));
        if (jaExiste) {
            throw new RegraNegocioException("Este livro já está cadastrado no BookSwipe");
        }

        SugestaoLivro completa = detalharSemFalhar(sugestao);
        Livro livro = new Livro(completa.titulo(), completa.autor(), genero);
        livro.setIsbn(completa.isbn());
        livro.setSinopse(completa.sinopse());
        livro.setCapaUrl(completa.capaUrl());
        livroDAO.inserir(livro);
        return livro;
    }

    // Procura na internet a capa (e ISBN/sinopse se faltarem) de um livro que já temos.
    // Não apaga o que já existe: a sinopse em português que escrevemos continua.
    public Livro completar(int livroId) throws SQLException {
        Livro livro = livroDAO.buscarPorId(livroId);
        if (livro == null) {
            throw new RegraNegocioException("Livro não encontrado");
        }
        Optional<SugestaoLivro> achado;
        try {
            achado = buscador.encontrar(livro.getTitulo(), livro.getAutor());
        } catch (IOException e) {
            throw new RegraNegocioException("Não consegui consultar a " + fonte() + ": " + e.getMessage());
        }
        if (achado.isEmpty()) {
            throw new RegraNegocioException("Não encontrei \"" + livro.getTitulo() + "\" na " + fonte());
        }

        SugestaoLivro s = achado.get();
        if (livro.getCapaUrl() == null) livro.setCapaUrl(s.capaUrl());
        if (livro.getIsbn() == null) livro.setIsbn(s.isbn());
        if (livro.getSinopse() == null) livro.setSinopse(s.sinopse());
        livroDAO.atualizar(livro);
        return livro;
    }

    // Completa todos os livros sem capa. Devolve quantos conseguiu.
    public int completarLivrosSemCapa() throws SQLException {
        int completados = 0;
        for (Livro livro : livroDAO.listarTodos()) {
            if (livro.getCapaUrl() != null) {
                continue;
            }
            try {
                completar(livro.getId());
                completados++;
            } catch (RegraNegocioException e) {
                System.out.println("  (pulei " + livro.getTitulo() + ": " + e.getMessage() + ")");
            }
            pausa(); // educação com a API de graça: não dispara tudo de uma vez
        }
        return completados;
    }

    private SugestaoLivro detalharSemFalhar(SugestaoLivro s) {
        try {
            return buscador.detalhar(s);
        } catch (IOException e) {
            return s; // sem sinopse, mas importa mesmo assim
        }
    }

    private void pausa() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
