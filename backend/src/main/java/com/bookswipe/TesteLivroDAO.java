package com.bookswipe;

import com.bookswipe.dao.GeneroDAO;
import com.bookswipe.dao.LivroDAO;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;

import java.sql.SQLException;

/**
 * Testa GeneroDAO e LivroDAO.
 * Cria um livro, pesquisa, altera e depois apaga (o banco volta como estava).
 */
public class TesteLivroDAO {

    public static void main(String[] args) {
        GeneroDAO generoDAO = new GeneroDAO();
        LivroDAO livroDAO = new LivroDAO();

        try {
            System.out.println("1) Gêneros:");
            for (Genero g : generoDAO.listarTodos()) {
                System.out.println("   " + g);
            }

            System.out.println("2) Livros (com o gênero vindo do JOIN):");
            for (Livro l : livroDAO.listarTodos()) {
                System.out.println("   " + l);
            }

            // CREATE - livro novo no gênero Fantasia
            Genero fantasia = generoDAO.buscarPorId(1);
            Livro novo = new Livro("O Senhor dos Anéis", "J.R.R. Tolkien", fantasia);
            livroDAO.inserir(novo);
            System.out.println("3) Inserido: " + novo);

            // READ - pesquisa por parte do nome
            System.out.println("4) Pesquisando \"tolkien\":");
            for (Livro l : livroDAO.pesquisar("tolkien")) {
                System.out.println("   " + l);
            }

            // UPDATE - muda o gênero para Ficção
            novo.setGenero(generoDAO.buscarPorId(4));
            livroDAO.atualizar(novo);
            System.out.println("5) Depois de atualizar: " + livroDAO.buscarPorId(novo.getId()));

            // DELETE
            livroDAO.excluir(novo.getId());
            System.out.println("6) Depois de excluir: " + livroDAO.buscarPorId(novo.getId()));

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
