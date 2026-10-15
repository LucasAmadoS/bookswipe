package com.bookswipe;

import com.bookswipe.dao.ExperienciaDAO;
import com.bookswipe.dao.LivroDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Experiencia;
import com.bookswipe.model.Livro;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;

/**
 * Testa o ExperienciaDAO.
 * Publica um post, mostra nas telas, edita e depois apaga (o banco volta como estava).
 */
public class TesteExperienciaDAO {

    public static void main(String[] args) {
        ExperienciaDAO dao = new ExperienciaDAO();

        try {
            Usuario lucas = new UsuarioDAO().buscarPorId(1);
            Usuario pedro = new UsuarioDAO().buscarPorId(6);
            Livro hobbit = new LivroDAO().buscarPorId(1);

            System.out.println("1) Tela Descobrir do Lucas (posts que ele ainda não avaliou):");
            for (Experiencia e : dao.listarParaDescobrir(lucas.getId())) {
                System.out.println("   " + e);
            }

            // CREATE - Pedro publica sobre O Hobbit
            Experiencia nova = new Experiencia(pedro, hobbit, 4, "Animado",
                    "Comecei pelo filme e o livro é ainda melhor.", null);
            dao.inserir(nova);
            System.out.println("2) Publicada experiência id " + nova.getId());

            System.out.println("3) Página do livro O Hobbit - experiências dos leitores:");
            for (Experiencia e : dao.listarPorLivro(hobbit.getId())) {
                System.out.println("   " + e);
            }

            // UPDATE
            nova.setNota(5);
            nova.setSentimento("Encantado");
            dao.atualizar(nova);
            System.out.println("4) Depois de editar: " + dao.buscarPorId(nova.getId()));

            // Validação do model: nota 7 não pode
            try {
                nova.setNota(7);
            } catch (IllegalArgumentException erro) {
                System.out.println("5) Tentou nota 7 -> bloqueado: " + erro.getMessage());
            }

            // DELETE (com transação)
            dao.excluir(nova.getId());
            System.out.println("6) Depois de excluir: " + dao.buscarPorId(nova.getId()));

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
