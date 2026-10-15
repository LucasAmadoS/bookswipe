package com.bookswipe;

import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.Senhas;

import java.sql.SQLException;

/**
 * Converte para BCrypt as senhas que ainda estão em texto puro no banco.
 * Rode UMA vez depois de ativar o hash. Pode rodar de novo sem problema:
 * quem já tem hash é pulado.
 */
public class MigrarSenhas {

    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();
        int convertidas = 0;

        try {
            for (Usuario u : dao.listarTodos()) {
                if (Senhas.ehHash(u.getSenha())) {
                    System.out.println("  já tem hash: " + u.getEmail());
                    continue;
                }
                u.setSenha(Senhas.gerarHash(u.getSenha())); // a senha continua a mesma, só muda como é guardada
                dao.atualizar(u);
                convertidas++;
                System.out.println("  convertida:  " + u.getEmail());
            }
            System.out.println(convertidas + " senha(s) convertida(s) para BCrypt.");
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
