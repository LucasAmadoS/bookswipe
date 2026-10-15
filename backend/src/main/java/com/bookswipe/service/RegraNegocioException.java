package com.bookswipe.service;

/**
 * Erro de regra do BookSwipe (ex: "você não pode pedir o próprio livro").
 * Herda de RuntimeException: é uma exceção nossa, com nome que explica o problema.
 * O console e a API mostram a mensagem dela para o usuário.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
