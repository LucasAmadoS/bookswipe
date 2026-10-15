package com.bookswipe.api;

import com.bookswipe.api.Requisicoes.Importacao;
import com.bookswipe.api.Requisicoes.MudarEstante;
import com.bookswipe.api.Requisicoes.NovoLivro;
import com.bookswipe.model.StatusLeitura;
import com.bookswipe.model.SugestaoLivro;
import com.bookswipe.service.CatalogoExternoService;
import com.bookswipe.service.LivroService;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;

import java.util.Map;

/**
 * Rotas do catálogo de livros e da "Minha estante".
 */
public class LivroController {

    private static final LivroService livros = new LivroService();
    private static final CatalogoExternoService catalogo = new CatalogoExternoService();

    public static void registrar(Javalin app) {

        // /api/livros?busca=hobbit  (sem busca = todos)
        app.get("/api/livros", ctx -> {
            String busca = ctx.queryParam("busca");
            ctx.json(livros.pesquisar(busca == null ? "" : busca));
        });

        app.get("/api/livros/{id}", ctx -> ctx.json(livros.buscar(Parametros.caminho(ctx, "id"))));

        app.post("/api/livros", ctx -> {
            NovoLivro d = ctx.bodyAsClass(NovoLivro.class);
            ctx.status(HttpStatus.CREATED).json(livros.cadastrar(d.titulo(), d.autor(), d.generoId()));
        });

        // ---------- API pública de livros (Open Library / Google Books) ----------

        // Busca na internet: /api/catalogo-externo?busca=dom casmurro
        app.get("/api/catalogo-externo", ctx -> ctx.json(Map.of(
                "fonte", catalogo.fonte(),
                "resultados", catalogo.buscar(ctx.queryParam("busca")))));

        // Salva no nosso banco um livro escolhido da busca
        app.post("/api/livros/importar", ctx -> {
            Importacao d = ctx.bodyAsClass(Importacao.class);
            SugestaoLivro sugestao = new SugestaoLivro(
                    d.titulo(), d.autor(), d.isbn(), d.sinopse(), d.capaUrl(), d.chaveExterna(), catalogo.fonte());
            ctx.status(HttpStatus.CREATED).json(catalogo.importar(sugestao, d.generoId()));
        });

        // Busca capa/ISBN/sinopse de um livro que já temos
        app.post("/api/livros/{id}/completar",
                ctx -> ctx.json(catalogo.completar(Parametros.caminho(ctx, "id"))));

        // ---------- Minha estante ----------

        // /api/usuarios/1/estante?aba=LIDO | LENDO | QUERO_LER | FAVORITOS  (sem aba = tudo)
        app.get("/api/usuarios/{id}/estante", ctx -> {
            int id = Autenticacao.dono(ctx);
            String aba = ctx.queryParam("aba");
            if (aba == null || aba.isBlank()) {
                ctx.json(livros.estanteInteira(id));
            } else if (aba.equalsIgnoreCase("FAVORITOS")) {
                ctx.json(livros.favoritos(id));
            } else {
                ctx.json(livros.estante(id, statusDaAba(aba)));
            }
        });

        // Números das abas: {"QUERO_LER":1,"LENDO":1,"LIDO":3}
        app.get("/api/usuarios/{id}/estante/totais",
                ctx -> ctx.json(livros.totaisDaEstante(Autenticacao.dono(ctx))));

        // Botões "Quero ler / Lendo / Já li" da página do livro
        app.put("/api/usuarios/{id}/estante/{livroId}", ctx -> {
            StatusLeitura status = ctx.bodyAsClass(MudarEstante.class).status();
            livros.colocarNaEstante(Autenticacao.dono(ctx), Parametros.caminho(ctx, "livroId"), status);
            ctx.status(HttpStatus.NO_CONTENT); // 204: deu certo, nada para devolver
        });

        // Botão ❤️ (liga/desliga)
        app.post("/api/usuarios/{id}/estante/{livroId}/favorito", ctx -> {
            boolean favorito = livros.alternarFavorito(Autenticacao.dono(ctx), Parametros.caminho(ctx, "livroId"));
            ctx.json(Map.of("favorito", favorito));
        });

        app.delete("/api/usuarios/{id}/estante/{livroId}", ctx -> {
            livros.removerDaEstante(Autenticacao.dono(ctx), Parametros.caminho(ctx, "livroId"));
            ctx.status(HttpStatus.NO_CONTENT);
        });
    }

    private static StatusLeitura statusDaAba(String aba) {
        try {
            return StatusLeitura.valueOf(aba.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse("Aba inválida. Use LIDO, LENDO, QUERO_LER ou FAVORITOS");
        }
    }
}
