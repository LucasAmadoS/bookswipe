package com.bookswipe.model;

/**
 * Um livro encontrado numa API pública (Open Library ou Google Books), ainda não salvo no nosso banco.
 * Campos podem vir vazios (null): nem todo livro tem ISBN, capa ou sinopse.
 */
public record SugestaoLivro(
        String titulo,
        String autor,
        String isbn,
        String sinopse,
        String capaUrl,
        String chaveExterna, // o id do livro na API de fora (para buscar mais detalhes depois)
        String fonte) {      // "Open Library" ou "Google Books"

    // Cópia com a sinopse preenchida (record não muda: cria outro)
    public SugestaoLivro comSinopse(String novaSinopse) {
        return new SugestaoLivro(titulo, autor, isbn, novaSinopse, capaUrl, chaveExterna, fonte);
    }
}
