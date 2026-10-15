package com.bookswipe.console;

import com.bookswipe.model.CondicaoLivro;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;
import com.bookswipe.model.LivroDisponivel;
import com.bookswipe.model.SolicitacaoTroca;
import com.bookswipe.model.StatusSolicitacao;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.LivroService;
import com.bookswipe.service.TrocaService;

import java.sql.SQLException;
import java.util.List;

/**
 * ♻️ Marketplace de trocas: ver livros, pedir, oferecer e responder pedidos.
 */
public class MenuTrocas {

    private static final TrocaService trocas = new TrocaService();
    private static final LivroService livros = new LivroService();

    public static void abrir(Usuario usuario) {
        while (true) {
            Tela.titulo("♻️  Livros para troca");
            System.out.println("1 - 🔍 Ver livros disponíveis e pedir");
            System.out.println("2 - 📤 Oferecer um livro meu");
            System.out.println("3 - 📥 Pedidos recebidos (aceitar / recusar / concluir)");
            System.out.println("4 - 🕓 Meus pedidos");
            System.out.println("5 - 📚 Meus livros oferecidos");
            System.out.println("0 - Voltar");

            switch (Entrada.numero("Opção", 0, 5)) {
                case 1 -> Tela.executar(() -> marketplace(usuario));
                case 2 -> Tela.executar(() -> oferecer(usuario));
                case 3 -> Tela.executar(() -> pedidosRecebidos(usuario));
                case 4 -> Tela.executar(() -> meusPedidos(usuario));
                case 5 -> Tela.executar(() -> meusOferecidos(usuario));
                case 0 -> {
                    return;
                }
            }
        }
    }

    private static void marketplace(Usuario usuario) throws SQLException {
        Tela.secao("Filtrar por gênero");
        System.out.println("  0 - Todos");
        for (Genero g : livros.generos()) {
            System.out.println("  " + g);
        }
        int generoId = Entrada.naoNegativo("Gênero");

        List<LivroDisponivel> lista = trocas.marketplace(usuario.getId(), generoId);
        Tela.secao("♻️  Livros disponíveis");
        if (lista.isEmpty()) {
            System.out.println("  Nenhum livro disponível nesse filtro.");
            Entrada.pausar();
            return;
        }
        for (LivroDisponivel ld : lista) {
            System.out.println("  " + ld.getId() + " - 📖 " + ld.getLivro().getTitulo()
                    + "  · 🟢 " + ld.getCondicao() + "  · 📍 " + ld.getDono().getBairro()
                    + "  · dono: " + ld.getDono().getNome());
        }

        int id = Entrada.id("\nNúmero do livro para solicitar troca");
        if (id == 0) {
            return;
        }
        if (Entrada.simNao("Você deseja solicitar este livro?")) {
            SolicitacaoTroca pedido = trocas.solicitar(usuario.getId(), id);
            Tela.ok("Solicitação enviada! Agora aguarde " + pedido.getLivroDisponivel().getDono().getNome()
                    + " aceitar.");
            System.out.println("  Status: 🟡 Aguardando resposta");
            Entrada.pausar();
        }
    }

    private static void oferecer(Usuario usuario) throws SQLException {
        Tela.secao("📤 Qual livro você quer colocar para circular?");
        Livro livro = MenuLivros.escolherLivro();
        if (livro == null) {
            return;
        }
        System.out.println("Condição:  1 - NOVO   2 - BOA   3 - REGULAR");
        CondicaoLivro condicao = CondicaoLivro.values()[Entrada.numero("Condição", 1, 3) - 1];

        trocas.oferecer(usuario.getId(), livro.getId(), condicao);
        Tela.ok(livro.getTitulo() + " está disponível para troca! ♻️");
        Entrada.pausar();
    }

    private static void pedidosRecebidos(Usuario usuario) throws SQLException {
        List<SolicitacaoTroca> pedidos = trocas.pedidosRecebidos(usuario.getId());
        Tela.secao("📥 Pedidos recebidos");
        if (pedidos.isEmpty()) {
            System.out.println("  Ninguém pediu seus livros ainda.");
            Entrada.pausar();
            return;
        }
        for (SolicitacaoTroca p : pedidos) {
            System.out.println("  " + p + iconeStatus(p.getStatus()));
        }

        int id = Entrada.id("\nNúmero do pedido (#) para responder");
        if (id == 0) {
            return;
        }
        System.out.println("1 - ✅ Aceitar   2 - ❌ Recusar   3 - ♻️ Concluir (já entreguei o livro)   0 - Voltar");
        switch (Entrada.numero("Opção", 0, 3)) {
            case 1 -> {
                trocas.aceitar(usuario.getId(), id);
                Tela.ok("Pedido aceito! O livro está RESERVADO. Combine a entrega e depois conclua a troca.");
            }
            case 2 -> {
                trocas.recusar(usuario.getId(), id);
                Tela.ok("Pedido recusado.");
            }
            case 3 -> {
                trocas.concluir(usuario.getId(), id);
                Tela.ok("Troca concluída! Mais um livro voltou a circular. ♻️");
            }
            default -> { }
        }
        Entrada.pausar();
    }

    private static void meusPedidos(Usuario usuario) throws SQLException {
        Tela.secao("🕓 Meus pedidos");
        List<SolicitacaoTroca> pedidos = trocas.meusPedidos(usuario.getId());
        if (pedidos.isEmpty()) {
            System.out.println("  Você ainda não pediu nenhum livro.");
        }
        for (SolicitacaoTroca p : pedidos) {
            System.out.println("  " + p + iconeStatus(p.getStatus()));
        }
        Entrada.pausar();
    }

    private static void meusOferecidos(Usuario usuario) throws SQLException {
        Tela.secao("📚 Meus livros oferecidos");
        List<LivroDisponivel> lista = trocas.meusOferecidos(usuario.getId());
        if (lista.isEmpty()) {
            System.out.println("  Você ainda não ofereceu nenhum livro.");
        }
        for (LivroDisponivel ld : lista) {
            System.out.println("  " + ld.getLivro().getTitulo() + " - " + ld.getCondicao() + " - " + ld.getStatus());
        }
        Entrada.pausar();
    }

    private static String iconeStatus(StatusSolicitacao status) {
        return switch (status) {
            case PENDENTE -> "  🟡";
            case ACEITA -> "  🟢";
            case RECUSADA -> "  🔴";
            case CONCLUIDA -> "  ♻️";
        };
    }
}
