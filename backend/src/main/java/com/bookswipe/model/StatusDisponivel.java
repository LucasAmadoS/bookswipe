package com.bookswipe.model;

/**
 * Situação do exemplar no marketplace.
 * DISPONIVEL -> RESERVADO (dono aceitou um pedido) -> TROCADO (troca concluída)
 */
public enum StatusDisponivel {
    DISPONIVEL,
    RESERVADO,
    TROCADO
}
