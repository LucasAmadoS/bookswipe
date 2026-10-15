package com.bookswipe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * Um aluno que usa o BookSwipe. Espelha a tabela "usuario".
 */
public class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senha;
    private String curso;
    private String bairro;
    private LocalDateTime dataCadastro;

    public Usuario() {
    }

    // Construtor para cadastrar um usuário novo (id e data o banco gera)
    public Usuario(String nome, String email, String senha, String curso, String bairro) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.curso = curso;
        this.bairro = bairro;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // @JsonIgnore: a senha NUNCA vai no JSON que a API devolve para o site
    @JsonIgnore
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Override
    public String toString() {
        return id + " - " + nome + " (" + email + ") - " + bairro;
    }
}
