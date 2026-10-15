package com.bookswipe.model;

/**
 * Um exemplar físico que alguém oferece para troca. Espelha a tabela "livro_disponivel".
 * Livro = a obra (O Hobbit). LivroDisponivel = o O Hobbit do Lucas, em bom estado.
 */
public class LivroDisponivel {

    private int id;
    private Usuario dono;
    private Livro livro;
    private CondicaoLivro condicao;
    private StatusDisponivel status;

    public LivroDisponivel() {
    }

    // Construtor para oferecer um livro (todo exemplar novo começa DISPONIVEL)
    public LivroDisponivel(Usuario dono, Livro livro, CondicaoLivro condicao) {
        this.dono = dono;
        this.livro = livro;
        this.condicao = condicao;
        this.status = StatusDisponivel.DISPONIVEL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getDono() {
        return dono;
    }

    public void setDono(Usuario dono) {
        this.dono = dono;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public CondicaoLivro getCondicao() {
        return condicao;
    }

    public void setCondicao(CondicaoLivro condicao) {
        this.condicao = condicao;
    }

    public StatusDisponivel getStatus() {
        return status;
    }

    public void setStatus(StatusDisponivel status) {
        this.status = status;
    }

    // Ex: O Hobbit - dono: Lucas Amado (Centro) - BOA - DISPONIVEL
    @Override
    public String toString() {
        return livro.getTitulo() + " - dono: " + dono.getNome() + " (" + dono.getBairro() + ") - "
                + condicao + " - " + status;
    }
}
