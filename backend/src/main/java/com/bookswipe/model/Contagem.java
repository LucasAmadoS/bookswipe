package com.bookswipe.model;

/**
 * Um rótulo e um número: "2026-06" -> 1 troca, "Lucas Amado" -> 2 trocas, "Fantasia" -> 6 posts.
 *
 * "record" é uma classe curta só para carregar dados: o Java cria sozinho
 * o construtor, os getters (rotulo() e total()), o equals e o toString.
 */
public record Contagem(String rotulo, int total) {
}
