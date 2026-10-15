package com.bookswipe.console;

import com.bookswipe.model.Contagem;
import com.bookswipe.model.Experiencia;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;
import com.bookswipe.model.Match;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.IndicadorService;
import com.bookswipe.service.IndicadorService.ImpactoUsuario;
import com.bookswipe.service.MatchService;
import com.bookswipe.service.SocialService;
import com.bookswipe.service.UsuarioService;

import java.sql.SQLException;
import java.util.List;

/**
 * Telas "Perfil", "Matches" e "Meu impacto".
 */
public class MenuPerfil {

    private static final UsuarioService usuarios = new UsuarioService();
    private static final SocialService social = new SocialService();
    private static final MatchService matchService = new MatchService();
    private static final IndicadorService indicadores = new IndicadorService();

    // 👤 Perfil
    public static void abrir(Usuario usuario) {
        while (true) {
            Tela.executar(() -> mostrarPerfil(usuario));
            System.out.println("\n1 - Editar meus gêneros favoritos   2 - Ver minhas experiências   0 - Voltar");
            switch (Entrada.numero("Opção", 0, 2)) {
                case 1 -> Tela.executar(() -> escolherGeneros(usuario));
                case 2 -> Tela.executar(() -> minhasExperiencias(usuario));
                case 0 -> {
                    return;
                }
            }
        }
    }

    private static void mostrarPerfil(Usuario usuario) throws SQLException {
        ImpactoUsuario impacto = indicadores.impactoDoUsuario(usuario.getId());
        List<Genero> generos = usuarios.generosDoUsuario(usuario.getId());

        Tela.titulo("👤 " + usuario.getNome());
        System.out.println("  " + usuario.getEmail() + "  ·  " + usuario.getCurso() + "  ·  📍 " + usuario.getBairro());
        System.out.println();
        System.out.println("  📚 " + impacto.livrosLidos() + " livros lidos    ❤️ " + impacto.favoritos()
                + " favoritos    ♻️ " + impacto.livrosReutilizados() + " livros reutilizados");
        System.out.println("\n  Meus gêneros: " + String.join(" · ", generos.stream().map(Genero::getNome).toList()));
    }

    private static void minhasExperiencias(Usuario usuario) throws SQLException {
        Tela.secao("✍️  Minhas experiências");
        List<Experiencia> minhas = social.experienciasDoUsuario(usuario.getId());
        if (minhas.isEmpty()) {
            System.out.println("  Você ainda não publicou nenhuma experiência.");
        }
        for (Experiencia e : minhas) {
            System.out.println("  📖 " + e.getLivro().getTitulo() + "  " + Tela.estrelas(e.getNota())
                    + "   ❤️ " + e.getTotalGostei() + "  💬 " + e.getTotalComentarios());
        }
        Entrada.pausar();
    }

    // Usado no cadastro e no "editar gêneros"
    public static void escolherGeneros(Usuario usuario) throws SQLException {
        Tela.secao("Qual gênero você mais gosta?");
        for (Genero g : usuarios.todosOsGeneros()) {
            System.out.println("  " + g);
        }
        List<Integer> escolhidos = Entrada.listaDeNumeros("Escolha seus favoritos (ex: 1, 4, 5)");
        usuarios.definirGeneros(usuario.getId(), escolhidos);
        Tela.ok("Gêneros salvos! Eles ajudam a encontrar seus matches.");
    }

    // ❤️ Matches
    public static void matches(Usuario usuario) throws SQLException {
        Tela.titulo("❤️  Seus matches literários");
        System.out.println("Pessoas que gostam dos mesmos livros\n");
        List<Match> lista = matchService.buscarMatches(usuario.getId());
        if (lista.isEmpty()) {
            System.out.println("  Ainda sem matches. Curta experiências e marque livros como lidos!");
        }
        for (Match m : lista) {
            System.out.println("  👤 " + m.getUsuario().getNome() + "   " + m.getAfinidade() + "% de afinidade ❤️");
            for (Livro l : m.getLivrosEmComum()) {
                System.out.println("     📖 " + l.getTitulo());
            }
            if (!m.getGenerosEmComum().isEmpty()) {
                System.out.println("     🏷️  " + String.join(" · ",
                        m.getGenerosEmComum().stream().map(Genero::getNome).toList()));
            }
            System.out.println();
        }
        Entrada.pausar();
    }

    // 📊 Meu impacto + a comunidade
    public static void meuImpacto(Usuario usuario) throws SQLException {
        ImpactoUsuario meu = indicadores.impactoDoUsuario(usuario.getId());

        Tela.titulo("♻️  Seu impacto");
        System.out.println("Você está ajudando livros a continuarem circulando.\n");
        System.out.println("  ♻️ " + meu.livrosReutilizados() + " livros reutilizados por você");

        Tela.secao("🌎 Comunidade BookSwipe");
        System.out.println("  " + indicadores.livrosReutilizados() + " livros reutilizados   ·   "
                + indicadores.taxaReuso() + "% taxa de reuso   ·   "
                + indicadores.taxaEngajamento() + " interações por post");

        Tela.secao("📚 Livros reutilizados por mês");
        for (Contagem c : indicadores.trocasPorMes()) {
            System.out.println("  " + c.rotulo() + "  " + Tela.barra(c.total(), 4) + " " + c.total());
        }

        Tela.secao("🏆 Ranking da comunidade");
        List<Contagem> ranking = indicadores.rankingComunidade(3);
        for (int i = 0; i < ranking.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + ranking.get(i).rotulo() + "   " + ranking.get(i).total() + " trocas");
        }

        Tela.secao("🔥 Em alta");
        for (Contagem c : indicadores.emAlta(3)) {
            System.out.println("  📖 " + c.rotulo() + "   ❤️ " + c.total());
        }
        Entrada.pausar();
    }
}
