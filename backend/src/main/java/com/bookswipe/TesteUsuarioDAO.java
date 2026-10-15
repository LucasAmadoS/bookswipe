package com.bookswipe;

import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;

/**
 * Testa o CRUD completo do UsuarioDAO.
 * Cria um usuário, lista, altera e depois apaga (o banco volta como estava).
 */
public class TesteUsuarioDAO {

    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();

        try {
            // CREATE
            Usuario novo = new Usuario("Teste da Silva", "teste@bookswipe.com", "123", "ADS", "Centro");
            dao.inserir(novo);
            System.out.println("1) Inserido com id " + novo.getId());

            // READ
            System.out.println("2) Todos os usuários:");
            for (Usuario u : dao.listarTodos()) {
                System.out.println("   " + u);
            }

            // UPDATE
            novo.setBairro("Campolim");
            dao.atualizar(novo);
            System.out.println("3) Depois de atualizar: " + dao.buscarPorId(novo.getId()));

            // DELETE
            dao.excluir(novo.getId());
            System.out.println("4) Depois de excluir, buscar o id " + novo.getId() + " retorna: "
                    + dao.buscarPorId(novo.getId()));

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
