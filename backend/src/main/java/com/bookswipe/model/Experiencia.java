package com.bookswipe.model;

import java.time.LocalDateTime;

/**
 * O "post": a experiência de um usuário lendo um livro. Espelha a tabela "experiencia".
 */
public class Experiencia {

    private int id;
    private Usuario usuario; // quem escreveu
    private Livro livro;     // sobre qual livro
    private int nota;        // 0 a 5
    private String sentimento;
    private String texto;
    private String trechoFavorito;
    private LocalDateTime dataPost;

    // Calculados pelo banco (COUNT) - só leitura, não existem na tabela
    private int totalGostei;
    private int totalComentarios;

    public Experiencia() {
    }

    // Construtor para publicar uma experiência nova
    public Experiencia(Usuario usuario, Livro livro, int nota, String sentimento,
                       String texto, String trechoFavorito) {
        this.usuario = usuario;
        this.livro = livro;
        setNota(nota); // passa pela validação
        this.sentimento = sentimento;
        this.texto = texto;
        this.trechoFavorito = trechoFavorito;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public int getNota() {
        return nota;
    }

    // Encapsulamento na prática: ninguém consegue colocar nota inválida
    public void setNota(int nota) {
        if (nota < 0 || nota > 5) {
            throw new IllegalArgumentException("A nota deve ser de 0 a 5");
        }
        this.nota = nota;
    }

    public String getSentimento() {
        return sentimento;
    }

    public void setSentimento(String sentimento) {
        this.sentimento = sentimento;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTrechoFavorito() {
        return trechoFavorito;
    }

    public void setTrechoFavorito(String trechoFavorito) {
        this.trechoFavorito = trechoFavorito;
    }

    public LocalDateTime getDataPost() {
        return dataPost;
    }

    public void setDataPost(LocalDateTime dataPost) {
        this.dataPost = dataPost;
    }

    public int getTotalGostei() {
        return totalGostei;
    }

    public void setTotalGostei(int totalGostei) {
        this.totalGostei = totalGostei;
    }

    public int getTotalComentarios() {
        return totalComentarios;
    }

    public void setTotalComentarios(int totalComentarios) {
        this.totalComentarios = totalComentarios;
    }

    // Ex: "O Hobbit" por Lucas Amado - ★★★★★ (Empolgado) | ❤ 3  💬 2
    @Override
    public String toString() {
        String estrelas = "★".repeat(nota) + "☆".repeat(5 - nota);
        return "\"" + livro.getTitulo() + "\" por " + usuario.getNome()
                + " - " + estrelas + " (" + sentimento + ")"
                + " | ❤ " + totalGostei + "  💬 " + totalComentarios;
    }
}
