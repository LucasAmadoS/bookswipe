package com.bookswipe.api;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

/**
 * Lê números da URL. Ex: em /api/livros/3, o {id} é "3" (texto) e vira 3 (int).
 * Se vier algo que não é número (/api/livros/abc), responde 400 em vez de dar erro no servidor.
 */
public class Parametros {

    // Parte do caminho: /api/livros/{id}
    public static int caminho(Context ctx, String nome) {
        try {
            return Integer.parseInt(ctx.pathParam(nome));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("O parâmetro '" + nome + "' precisa ser um número");
        }
    }

    // Parte depois do "?": /api/trocas/disponiveis?generoId=1  (se não vier, usa o padrão)
    public static int consulta(Context ctx, String nome, int padrao) {
        String valor = ctx.queryParam(nome);
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("O parâmetro '" + nome + "' precisa ser um número");
        }
    }
}
