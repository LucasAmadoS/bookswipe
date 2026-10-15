package com.bookswipe.model;

import java.util.List;

/**
 * Um "match literário": outro leitor e o quanto ele combina com você.
 * Não é tabela: é calculado na hora pelo MatchService.
 */
public class Match {

    private final Usuario usuario;
    private final int afinidade; // 0 a 100 (%)
    private final List<Livro> livrosEmComum;
    private final List<Genero> generosEmComum;

    public Match(Usuario usuario, int afinidade, List<Livro> livrosEmComum, List<Genero> generosEmComum) {
        this.usuario = usuario;
        this.afinidade = afinidade;
        this.livrosEmComum = livrosEmComum;
        this.generosEmComum = generosEmComum;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getAfinidade() {
        return afinidade;
    }

    public List<Livro> getLivrosEmComum() {
        return livrosEmComum;
    }

    public List<Genero> getGenerosEmComum() {
        return generosEmComum;
    }

    // Ex: Ana Souza - 37% de afinidade ❤ | livros: O Hobbit, Harry Potter... | gêneros: Fantasia, Ficção
    @Override
    public String toString() {
        String livros = String.join(", ", livrosEmComum.stream().map(Livro::getTitulo).toList());
        String generos = String.join(", ", generosEmComum.stream().map(Genero::getNome).toList());
        return usuario.getNome() + " - " + afinidade + "% de afinidade ❤"
                + " | livros: " + (livros.isEmpty() ? "-" : livros)
                + " | gêneros: " + (generos.isEmpty() ? "-" : generos);
    }
}
