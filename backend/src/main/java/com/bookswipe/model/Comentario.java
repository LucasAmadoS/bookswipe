package com.bookswipe.model;

import java.time.LocalDateTime;

/**
 * Um comentário em uma experiência. Espelha a tabela "comentario".
 */
public class Comentario {

    private int id;
    private Usuario usuario;   // quem comentou
    private int experienciaId; // em qual post
    private String texto;
    private LocalDateTime dataComentario;

    public Comentario() {
    }

    public Comentario(Usuario usuario, int experienciaId, String texto) {
        this.usuario = usuario;
        this.experienciaId = experienciaId;
        setTexto(texto); // passa pela validação
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

    public int getExperienciaId() {
        return experienciaId;
    }

    public void setExperienciaId(int experienciaId) {
        this.experienciaId = experienciaId;
    }

    public String getTexto() {
        return texto;
    }

    // Comentário vazio não pode, e a coluna aceita no máximo 1000 caracteres
    public void setTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("O comentário não pode ficar vazio");
        }
        if (texto.length() > 1000) {
            throw new IllegalArgumentException("O comentário pode ter no máximo 1000 caracteres");
        }
        this.texto = texto;
    }

    public LocalDateTime getDataComentario() {
        return dataComentario;
    }

    public void setDataComentario(LocalDateTime dataComentario) {
        this.dataComentario = dataComentario;
    }

    @Override
    public String toString() {
        return usuario.getNome() + ": " + texto;
    }
}
