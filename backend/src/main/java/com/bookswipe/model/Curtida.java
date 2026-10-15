package com.bookswipe.model;

import java.time.LocalDateTime;

/**
 * Um swipe: o usuário deu ❤ GOSTEI ou ❌ PASSAR em uma experiência. Espelha a tabela "curtida".
 */
public class Curtida {

    private int id;
    private int usuarioId;
    private int experienciaId;
    private TipoCurtida tipo;
    private LocalDateTime dataCurtida;

    public Curtida() {
    }

    public Curtida(int usuarioId, int experienciaId, TipoCurtida tipo) {
        this.usuarioId = usuarioId;
        this.experienciaId = experienciaId;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getExperienciaId() {
        return experienciaId;
    }

    public void setExperienciaId(int experienciaId) {
        this.experienciaId = experienciaId;
    }

    public TipoCurtida getTipo() {
        return tipo;
    }

    public void setTipo(TipoCurtida tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataCurtida() {
        return dataCurtida;
    }

    public void setDataCurtida(LocalDateTime dataCurtida) {
        this.dataCurtida = dataCurtida;
    }

    @Override
    public String toString() {
        return "usuário " + usuarioId + " -> experiência " + experienciaId + ": " + tipo;
    }
}
