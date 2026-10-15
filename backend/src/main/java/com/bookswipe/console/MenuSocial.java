package com.bookswipe.console;

import com.bookswipe.model.Comentario;
import com.bookswipe.model.Experiencia;
import com.bookswipe.model.Livro;
import com.bookswipe.model.TipoCurtida;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.SocialService;

import java.sql.SQLException;
import java.util.List;

/**
 * Telas "Descobrir" (o swipe) e "Compartilhe sua experiência".
 */
public class MenuSocial {

    private static final SocialService social = new SocialService();

    // 🔥 Descobrir: um card por vez, como no Tinder
    public static void descobrir(Usuario usuario) throws SQLException {
        List<Experiencia> fila = social.descobrir(usuario.getId());
        if (fila.isEmpty()) {
            Tela.titulo("🔥 Descobrir");
            System.out.println("Você já viu todas as experiências! Volte mais tarde. 📚");
            Entrada.pausar();
            return;
        }

        for (int i = 0; i < fila.size(); i++) {
            Experiencia exp = fila.get(i);
            Tela.titulo("🔥 Descobrir  (" + (i + 1) + " de " + fila.size() + ")");
            mostrarCard(exp);

            boolean decidiu = false;
            while (!decidiu) {
                System.out.println("\n  1 - ❤️  Gostei    2 - ❌ Passar    3 - 💬 Comentários    0 - Voltar");
                switch (Entrada.numero("Opção", 0, 3)) {
                    case 1 -> {
                        social.avaliar(usuario.getId(), exp.getId(), TipoCurtida.GOSTEI);
                        System.out.println("\n           ❤️  Você gostou!");
                        decidiu = true;
                    }
                    case 2 -> {
                        social.avaliar(usuario.getId(), exp.getId(), TipoCurtida.PASSAR);
                        decidiu = true;
                    }
                    case 3 -> comentarios(usuario, exp);
                    case 0 -> {
                        return;
                    }
                }
            }
        }
        System.out.println("\nFim da fila! Você viu todas as experiências novas. ✨");
        Entrada.pausar();
    }

    // O card grande do protótipo
    public static void mostrarCard(Experiencia exp) {
        System.out.println("  📖 " + exp.getLivro().getTitulo().toUpperCase());
        System.out.println("     " + exp.getLivro().getAutor());
        System.out.println();
        System.out.println("  " + Tela.estrelas(exp.getNota()) + "   por " + exp.getUsuario().getNome()
                + (exp.getSentimento() != null ? " · " + exp.getSentimento() : ""));
        if (exp.getTexto() != null) {
            System.out.println("  \"" + exp.getTexto() + "\"");
        }
        if (exp.getTrechoFavorito() != null) {
            System.out.println("  ✨ Trecho favorito: \"" + exp.getTrechoFavorito() + "\"");
        }
        System.out.println();
        System.out.println("  ❤️ " + exp.getTotalGostei() + " curtiram   💬 " + exp.getTotalComentarios() + " comentários");
    }

    // Lista os comentários e deixa comentar
    public static void comentarios(Usuario usuario, Experiencia exp) throws SQLException {
        Tela.secao("💬 Comentários");
        List<Comentario> lista = social.comentarios(exp.getId());
        if (lista.isEmpty()) {
            System.out.println("Ninguém comentou ainda. Seja o primeiro!");
        }
        for (Comentario c : lista) {
            System.out.println("  " + c.getUsuario().getNome() + ": " + c.getTexto());
        }
        String texto = Entrada.texto("\nSeu comentário (Enter para não comentar)");
        if (!texto.isEmpty()) {
            social.comentar(usuario.getId(), exp.getId(), texto);
            Tela.ok("Comentário publicado");
        }
    }

    // ✍️ Compartilhe sua experiência
    public static void publicar(Usuario usuario) throws SQLException {
        Tela.titulo("✍️  Compartilhe sua experiência");
        Livro livro = MenuLivros.escolherLivro();
        if (livro == null) {
            return;
        }
        System.out.println("\n📖 " + livro.getTitulo() + " — " + livro.getAutor());
        int nota = Entrada.numero("Sua nota", 0, 5);
        String sentimento = Entrada.texto("Como você se sentiu? (ex: Empolgado, Reflexivo)");
        String texto = Entrada.textoObrigatorio("Como foi sua experiência?");
        String trecho = Entrada.texto("Seu trecho favorito (opcional)");

        social.publicar(usuario.getId(), livro.getId(), nota,
                sentimento.isEmpty() ? null : sentimento, texto, trecho.isEmpty() ? null : trecho);
        Tela.ok("Experiência publicada! O livro foi para a sua estante como LIDO.");
        Entrada.pausar();
    }
}
