package com.bookswipe.console;

import com.bookswipe.model.Experiencia;
import com.bookswipe.model.Genero;
import com.bookswipe.model.ItemEstante;
import com.bookswipe.model.Livro;
import com.bookswipe.model.StatusLeitura;
import com.bookswipe.model.SugestaoLivro;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.CatalogoExternoService;
import com.bookswipe.service.LivroService;
import com.bookswipe.service.SocialService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Pesquisa de livros, página do livro, cadastro de livro e "Minha estante".
 */
public class MenuLivros {

    private static final LivroService livros = new LivroService();
    private static final SocialService social = new SocialService();
    private static final CatalogoExternoService catalogo = new CatalogoExternoService();

    public static void abrir(Usuario usuario) {
        while (true) {
            Tela.titulo("📚 Livros");
            System.out.println("1 - 🔍 Pesquisar e ver um livro");
            System.out.println("2 - ➕ Cadastrar livro");
            System.out.println("3 - 📚 Minha estante");
            System.out.println("0 - Voltar");

            switch (Entrada.numero("Opção", 0, 3)) {
                case 1 -> Tela.executar(() -> {
                    Livro livro = escolherLivro();
                    if (livro != null) {
                        paginaDoLivro(usuario, livro);
                    }
                });
                case 2 -> Tela.executar(MenuLivros::cadastrar);
                case 3 -> estante(usuario);
                case 0 -> {
                    return;
                }
            }
        }
    }

    // Pesquisa e devolve o livro escolhido (null = desistiu). Usado também ao publicar e ao oferecer.
    public static Livro escolherLivro() throws SQLException {
        String busca = Entrada.texto("🔍 Pesquisar livro ou autor (Enter = todos)");
        List<Livro> encontrados = livros.pesquisar(busca);
        if (encontrados.isEmpty()) {
            Tela.aviso("Nenhum livro encontrado. Cadastre em Livros > Cadastrar livro.");
            return null;
        }
        for (Livro l : encontrados) {
            System.out.println("  " + l);
        }
        int id = Entrada.id("Número do livro");
        return (id == 0) ? null : livros.buscar(id);
    }

    // 📖 Página do livro
    private static void paginaDoLivro(Usuario usuario, Livro livro) throws SQLException {
        List<Experiencia> experiencias = social.experienciasDoLivro(livro.getId());
        double media = experiencias.stream().mapToInt(Experiencia::getNota).average().orElse(0);

        Tela.titulo("📖 " + livro.getTitulo());
        System.out.println("  " + livro.getAutor());
        System.out.printf("  ★ %.1f / 5  (%d experiências)%n", media, experiencias.size());
        if (livro.getGenero() != null) {
            System.out.println("  " + livro.getGenero().getNome());
        }
        if (livro.getSinopse() != null) {
            Tela.secao("Sobre o livro");
            System.out.println("  " + livro.getSinopse());
        }
        Tela.secao("Experiências dos leitores");
        if (experiencias.isEmpty()) {
            System.out.println("  Ninguém publicou ainda. Seja o primeiro!");
        }
        for (Experiencia e : experiencias) {
            System.out.println("  " + e.getUsuario().getNome() + "  " + Tela.estrelas(e.getNota()));
            System.out.println("  \"" + e.getTexto() + "\"");
            System.out.println("  ❤️ " + e.getTotalGostei() + "   💬 " + e.getTotalComentarios() + "\n");
        }

        System.out.println("1 - Quero ler   2 - Estou lendo   3 - Já li   4 - ❤️ Favoritar   0 - Voltar");
        switch (Entrada.numero("Opção", 0, 4)) {
            case 1 -> naEstante(usuario, livro, StatusLeitura.QUERO_LER);
            case 2 -> naEstante(usuario, livro, StatusLeitura.LENDO);
            case 3 -> naEstante(usuario, livro, StatusLeitura.LIDO);
            case 4 -> {
                boolean favorito = livros.alternarFavorito(usuario.getId(), livro.getId());
                Tela.ok(favorito ? "Adicionado aos favoritos ❤️" : "Removido dos favoritos");
            }
            default -> { }
        }
    }

    private static void naEstante(Usuario usuario, Livro livro, StatusLeitura status) throws SQLException {
        livros.colocarNaEstante(usuario.getId(), livro.getId(), status);
        Tela.ok(livro.getTitulo() + " está na sua estante como " + status);
    }

    private static void cadastrar() throws SQLException {
        Tela.secao("➕ Cadastrar livro");
        System.out.println("1 - 🌐 Buscar na internet (" + catalogo.fonte() + ": já vem com capa e ISBN)");
        System.out.println("2 - ⌨️  Digitar à mão");
        System.out.println("0 - Voltar");
        int opcao = Entrada.numero("Opção", 0, 2);
        if (opcao == 1) {
            importarDaInternet();
        } else if (opcao == 2) {
            digitarAMao();
        }
    }

    private static void importarDaInternet() throws SQLException {
        String busca = Entrada.textoObrigatorio("🌐 Título ou autor para buscar");
        List<SugestaoLivro> resultados = catalogo.buscar(busca);
        if (resultados.isEmpty()) {
            Tela.aviso("Nada encontrado na " + catalogo.fonte() + ". Tente outro texto ou digite à mão.");
            return;
        }
        for (int i = 0; i < resultados.size(); i++) {
            SugestaoLivro s = resultados.get(i);
            System.out.println("  " + (i + 1) + " - " + s.titulo() + " — " + s.autor()
                    + (s.capaUrl() != null ? "  🖼️" : ""));
        }
        int escolha = Entrada.numero("Qual é o seu livro? (0 para voltar)", 0, resultados.size());
        if (escolha == 0) {
            return;
        }
        int generoId = escolherGenero();
        if (generoId == 0) {
            return;
        }
        Livro novo = catalogo.importar(resultados.get(escolha - 1), generoId);
        Tela.ok("Livro cadastrado com dados da " + catalogo.fonte() + ": " + novo);
    }

    private static void digitarAMao() throws SQLException {
        String titulo = Entrada.textoObrigatorio("Título");
        String autor = Entrada.textoObrigatorio("Autor");
        int generoId = escolherGenero();
        if (generoId == 0) {
            return;
        }
        Livro novo = livros.cadastrar(titulo, autor, generoId);
        Tela.ok("Livro cadastrado: " + novo);
    }

    private static int escolherGenero() throws SQLException {
        for (Genero g : livros.generos()) {
            System.out.println("  " + g);
        }
        return Entrada.id("Número do gênero");
    }

    // 📚 Minha estante, com as abas do protótipo
    private static void estante(Usuario usuario) {
        while (true) {
            Tela.executar(() -> {
                Map<StatusLeitura, Integer> totais = livros.totaisDaEstante(usuario.getId());
                Tela.titulo("📚 Minha estante");
                System.out.println("1 - 📖 Lendo (" + totais.get(StatusLeitura.LENDO) + ")");
                System.out.println("2 - 📚 Lidos (" + totais.get(StatusLeitura.LIDO) + ")");
                System.out.println("3 - 🔖 Quero ler (" + totais.get(StatusLeitura.QUERO_LER) + ")");
                System.out.println("4 - ❤️  Favoritos (" + livros.favoritos(usuario.getId()).size() + ")");
                System.out.println("5 - Tirar um livro da estante");
                System.out.println("0 - Voltar");
            });

            int opcao = Entrada.numero("Opção", 0, 5);
            if (opcao == 0) {
                return;
            }
            Tela.executar(() -> {
                if (opcao == 5) {
                    tirarDaEstante(usuario);
                    return;
                }
                List<ItemEstante> itens = switch (opcao) {
                    case 1 -> livros.estante(usuario.getId(), StatusLeitura.LENDO);
                    case 2 -> livros.estante(usuario.getId(), StatusLeitura.LIDO);
                    case 3 -> livros.estante(usuario.getId(), StatusLeitura.QUERO_LER);
                    default -> livros.favoritos(usuario.getId());
                };
                Tela.secao("Seus livros");
                if (itens.isEmpty()) {
                    System.out.println("  Nenhum livro aqui ainda.");
                }
                for (ItemEstante item : itens) {
                    System.out.println("  " + item.getLivro().getId() + " - " + item);
                }
                Entrada.pausar();
            });
        }
    }

    private static void tirarDaEstante(Usuario usuario) throws SQLException {
        Tela.secao("Sua estante");
        for (ItemEstante item : livros.estanteInteira(usuario.getId())) {
            System.out.println("  " + item.getLivro().getId() + " - " + item);
        }
        int livroId = Entrada.id("Número do livro para tirar da estante");
        if (livroId > 0) {
            livros.removerDaEstante(usuario.getId(), livroId);
            Tela.ok("Livro retirado da estante");
        }
    }
}
