package com.bookswipe.api;

import com.bookswipe.api.Requisicoes.Avaliacao;
import com.bookswipe.api.Requisicoes.NovaExperiencia;
import com.bookswipe.api.Requisicoes.NovoComentario;
import com.bookswipe.service.MatchService;
import com.bookswipe.service.SocialService;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

/**
 * Rotas da rede social: descobrir (swipe), experiências, comentários e matches.
 * Quem curte, comenta ou publica é sempre o usuário do token (Autenticacao.usuario).
 */
public class SocialController {

    private static final SocialService social = new SocialService();
    private static final MatchService matches = new MatchService();

    public static void registrar(Javalin app) {

        // Fila da tela Descobrir
        app.get("/api/usuarios/{id}/descobrir",
                ctx -> ctx.json(social.descobrir(Autenticacao.dono(ctx))));

        // Perfil: "Minhas experiências"
        app.get("/api/usuarios/{id}/experiencias",
                ctx -> ctx.json(social.experienciasDoUsuario(Autenticacao.dono(ctx))));

        // Página do livro: "Experiências dos leitores" (de todo mundo)
        app.get("/api/livros/{id}/experiencias",
                ctx -> ctx.json(social.experienciasDoLivro(Parametros.caminho(ctx, "id"))));

        app.post("/api/experiencias", ctx -> {
            NovaExperiencia d = ctx.bodyAsClass(NovaExperiencia.class);
            int eu = Autenticacao.usuario(ctx).getId();
            ctx.status(HttpStatus.CREATED).json(social.publicar(
                    eu, d.livroId(), d.nota(), d.sentimento(), d.texto(), d.trechoFavorito()));
        });

        // O swipe: {"tipo":"GOSTEI"} ou {"tipo":"PASSAR"}
        app.post("/api/experiencias/{id}/avaliacao", ctx -> {
            Avaliacao d = ctx.bodyAsClass(Avaliacao.class);
            social.avaliar(Autenticacao.usuario(ctx).getId(), Parametros.caminho(ctx, "id"), d.tipo());
            ctx.status(HttpStatus.NO_CONTENT);
        });

        app.get("/api/experiencias/{id}/comentarios",
                ctx -> ctx.json(social.comentarios(Parametros.caminho(ctx, "id"))));

        app.post("/api/experiencias/{id}/comentarios", ctx -> {
            NovoComentario d = ctx.bodyAsClass(NovoComentario.class);
            ctx.status(HttpStatus.CREATED).json(
                    social.comentar(Autenticacao.usuario(ctx).getId(), Parametros.caminho(ctx, "id"), d.texto()));
        });

        app.get("/api/usuarios/{id}/matches",
                ctx -> ctx.json(matches.buscarMatches(Autenticacao.dono(ctx))));
    }
}
