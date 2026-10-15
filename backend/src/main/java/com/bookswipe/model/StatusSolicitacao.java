package com.bookswipe.model;

/**
 * Situação de um pedido de troca.
 * PENDENTE -> ACEITA -> CONCLUIDA (o livro voltou a circular ♻️)
 * PENDENTE -> RECUSADA
 */
public enum StatusSolicitacao {
    PENDENTE,
    ACEITA,
    RECUSADA,
    CONCLUIDA
}
