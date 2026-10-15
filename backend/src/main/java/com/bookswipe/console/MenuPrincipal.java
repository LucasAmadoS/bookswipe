package com.bookswipe.console;

import com.bookswipe.model.Usuario;
import com.bookswipe.service.UsuarioService;

import java.sql.SQLException;

/**
 * Ponto de entrada do BookSwipe no console. Rode ESTA classe.
 * Tela inicial (entrar / criar conta) e o menu de quem está logado.
 */
public class MenuPrincipal {

    private static final UsuarioService usuarioService = new UsuarioService();

    public static void main(String[] args) {
        while (true) {
            Tela.titulo("📖 BookSwipe — Leia. Compartilhe. Troque.");
            System.out.println("Livros foram feitos para circular.\n");
            System.out.println("1 - Entrar");
            System.out.println("2 - Criar conta");
            System.out.println("0 - Sair");

            int opcao = Entrada.numero("Opção", 0, 2);
            if (opcao == 0) {
                System.out.println("\nAté a próxima leitura! 📚");
                return;
            }

            // Um array de 1 posição deixa a lambda guardar o usuário que fez login
            Usuario[] logado = new Usuario[1];
            if (opcao == 1) {
                Tela.executar(() -> logado[0] = entrar());
            } else {
                Tela.executar(() -> logado[0] = criarConta());
            }

            if (logado[0] != null) {
                menuDoUsuario(logado[0]);
            }
        }
    }

    private static Usuario entrar() throws SQLException {
        Tela.secao("Bem-vindo de volta! Continue sua próxima leitura.");
        String email = Entrada.textoObrigatorio("E-mail");
        String senha = Entrada.textoObrigatorio("Senha");
        return usuarioService.login(email, senha);
    }

    private static Usuario criarConta() throws SQLException {
        Tela.secao("Crie seu perfil de leitor");
        String nome = Entrada.textoObrigatorio("Como podemos chamar você");
        String email = Entrada.textoObrigatorio("E-mail");
        String senha = Entrada.textoObrigatorio("Senha (mínimo 6 caracteres)");
        String curso = Entrada.texto("Curso");
        String bairro = Entrada.texto("Bairro (aparece nas trocas)");

        Usuario novo = usuarioService.cadastrar(nome, email, senha, curso, bairro);
        Tela.ok("Conta criada! Agora conte do que você gosta.");
        MenuPerfil.escolherGeneros(novo);
        return novo;
    }

    // O menu lateral do protótipo
    private static void menuDoUsuario(Usuario usuario) {
        while (true) {
            Tela.titulo("Olá, " + usuario.getNome().split(" ")[0] + " 👋  O que você quer descobrir hoje?");
            System.out.println("1 - 🔥 Descobrir (swipe)");
            System.out.println("2 - ✍️  Publicar experiência");
            System.out.println("3 - 📚 Livros e minha estante");
            System.out.println("4 - ❤️  Matches");
            System.out.println("5 - ♻️  Trocar livros");
            System.out.println("6 - 📊 Meu impacto");
            System.out.println("7 - 👤 Meu perfil");
            System.out.println("0 - Sair da conta");

            switch (Entrada.numero("Opção", 0, 7)) {
                case 1 -> Tela.executar(() -> MenuSocial.descobrir(usuario));
                case 2 -> Tela.executar(() -> MenuSocial.publicar(usuario));
                case 3 -> MenuLivros.abrir(usuario);
                case 4 -> Tela.executar(() -> MenuPerfil.matches(usuario));
                case 5 -> MenuTrocas.abrir(usuario);
                case 6 -> Tela.executar(() -> MenuPerfil.meuImpacto(usuario));
                case 7 -> MenuPerfil.abrir(usuario);
                case 0 -> {
                    return;
                }
            }
        }
    }
}
