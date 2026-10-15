package com.bookswipe;

import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.RegraNegocioException;
import com.bookswipe.service.Senhas;
import com.bookswipe.service.UsuarioService;

import java.sql.SQLException;

/**
 * Testa o hash de senha: login certo, login errado e cadastro novo.
 * O usuário de teste criado é apagado no final.
 */
public class TesteSenhas {

    public static void main(String[] args) {
        UsuarioService service = new UsuarioService();
        UsuarioDAO dao = new UsuarioDAO();

        try {
            // 1) Mesma senha, hashes diferentes (por causa do sal)
            System.out.println("1) Dois hashes de \"123456\":");
            System.out.println("   " + Senhas.gerarHash("123456"));
            System.out.println("   " + Senhas.gerarHash("123456"));

            // 2) Login com a senha certa
            Usuario lucas = service.login("lucas@bookswipe.com", "123456");
            System.out.println("2) Login certo: OK, entrou como " + lucas.getNome());
            System.out.println("   No banco está guardado: " + dao.buscarPorId(lucas.getId()).getSenha());

            // 3) Login com a senha errada
            try {
                service.login("lucas@bookswipe.com", "654321");
                System.out.println("3) Senha errada: ENTROU (não devia!)");
            } catch (RegraNegocioException e) {
                System.out.println("3) Senha errada -> bloqueado: " + e.getMessage());
            }

            // 4) Cadastro novo já grava hash
            Usuario novo = service.cadastrar("Teste Hash", "hash@bookswipe.com", "minhaSenha1", "ADS", "Centro");
            String salvo = dao.buscarPorId(novo.getId()).getSenha();
            System.out.println("4) Cadastro novo guardou: " + salvo + "  (é hash? " + Senhas.ehHash(salvo) + ")");
            System.out.println("   Login do novo usuário: " + service.login("hash@bookswipe.com", "minhaSenha1").getNome());
            dao.excluir(novo.getId());
            System.out.println("   Usuário de teste apagado.");

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
