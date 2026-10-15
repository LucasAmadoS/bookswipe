package com.bookswipe.api;

import com.bookswipe.api.Requisicoes.Oferta;
import com.bookswipe.api.Requisicoes.Pedido;
import com.bookswipe.service.TrocaService;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

/**
 * Rotas do marketplace de trocas.
 * Quem oferece, pede, aceita ou recusa é sempre o usuário do token.
 * O TrocaService ainda confere se ele é o dono do livro (regra 4).
 */
public class TrocaController {

    private static final TrocaService trocas = new TrocaService();

    public static void registrar(Javalin app) {

        // Marketplace: /api/trocas/disponiveis?generoId=0 (sem os meus próprios livros)
        app.get("/api/trocas/disponiveis", ctx -> ctx.json(trocas.marketplace(
                Autenticacao.usuario(ctx).getId(),
                Parametros.consulta(ctx, "generoId", 0))));

        // Oferecer um livro: {"livroId":4,"condicao":"BOA"}
        app.post("/api/trocas/disponiveis", ctx -> {
            Oferta d = ctx.bodyAsClass(Oferta.class);
            ctx.status(HttpStatus.CREATED).json(
                    trocas.oferecer(Autenticacao.usuario(ctx).getId(), d.livroId(), d.condicao()));
        });

        app.get("/api/usuarios/{id}/livros-oferecidos",
                ctx -> ctx.json(trocas.meusOferecidos(Autenticacao.dono(ctx))));

        // Pedir um livro: {"livroDisponivelId":9}
        app.post("/api/trocas/pedidos", ctx -> {
            Pedido d = ctx.bodyAsClass(Pedido.class);
            ctx.status(HttpStatus.CREATED).json(
                    trocas.solicitar(Autenticacao.usuario(ctx).getId(), d.livroDisponivelId()));
        });

        app.get("/api/usuarios/{id}/pedidos-recebidos",
                ctx -> ctx.json(trocas.pedidosRecebidos(Autenticacao.dono(ctx))));

        app.get("/api/usuarios/{id}/pedidos-feitos",
                ctx -> ctx.json(trocas.meusPedidos(Autenticacao.dono(ctx))));

        // O dono responde (sem corpo: quem responde vem do token)
        app.put("/api/trocas/pedidos/{id}/aceitar", ctx -> {
            trocas.aceitar(Autenticacao.usuario(ctx).getId(), Parametros.caminho(ctx, "id"));
            ctx.status(HttpStatus.NO_CONTENT);
        });

        app.put("/api/trocas/pedidos/{id}/recusar", ctx -> {
            trocas.recusar(Autenticacao.usuario(ctx).getId(), Parametros.caminho(ctx, "id"));
            ctx.status(HttpStatus.NO_CONTENT);
        });

        app.put("/api/trocas/pedidos/{id}/concluir", ctx -> {
            trocas.concluir(Autenticacao.usuario(ctx).getId(), Parametros.caminho(ctx, "id"));
            ctx.status(HttpStatus.NO_CONTENT);
        });
    }
}
