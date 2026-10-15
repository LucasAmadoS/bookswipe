package com.bookswipe.model;

/**
 * Um livro na "Minha estante" de um usuário. Espelha a tabela "estante".
 * A chave é o par (usuário, livro): o mesmo livro não entra duas vezes na estante da mesma pessoa.
 */
public class ItemEstante {

    private int usuarioId;
    private Livro livro;
    private StatusLeitura status;
    private boolean favorito;

    public ItemEstante() {
    }

    public ItemEstante(int usuarioId, Livro livro, StatusLeitura status, boolean favorito) {
        this.usuarioId = usuarioId;
        this.livro = livro;
        this.status = status;
        this.favorito = favorito;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public StatusLeitura getStatus() {
        return status;
    }

    public void setStatus(StatusLeitura status) {
        this.status = status;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    @Override
    public String toString() {
        return livro.getTitulo() + " [" + status + "]" + (favorito ? " ❤ favorito" : "");
    }
}
