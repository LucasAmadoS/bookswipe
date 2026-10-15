package com.bookswipe.model;

/**
 * A obra (O Hobbit, 1984...). Espelha a tabela "livro".
 * Não confundir com LivroDisponivel, que é o exemplar físico de alguém.
 */
public class Livro {

    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String sinopse;
    private String capaUrl;
    private Genero genero; // associação: o livro TEM UM gênero

    public Livro() {
    }

    // Construtor para cadastrar um livro novo (o id o banco gera)
    public Livro(String titulo, String autor, Genero genero) {
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    @Override
    public String toString() {
        String nomeGenero = (genero != null) ? genero.getNome() : "sem gênero";
        return id + " - " + titulo + " (" + autor + ") [" + nomeGenero + "]";
    }
}
