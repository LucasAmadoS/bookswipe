package com.bookswipe.model;

import java.time.LocalDateTime;

/**
 * Um pedido de troca. Espelha a tabela "solicitacao_troca".
 * Quando o status vira CONCLUIDA, conta como 1 livro reutilizado (indicador de sustentabilidade).
 */
public class SolicitacaoTroca {

    private int id;
    private LivroDisponivel livroDisponivel; // o exemplar pedido (com o dono dentro)
    private Usuario solicitante;            // quem pediu
    private StatusSolicitacao status;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataConclusao;

    public SolicitacaoTroca() {
    }

    // Construtor para um pedido novo (todo pedido começa PENDENTE)
    public SolicitacaoTroca(LivroDisponivel livroDisponivel, Usuario solicitante) {
        this.livroDisponivel = livroDisponivel;
        this.solicitante = solicitante;
        this.status = StatusSolicitacao.PENDENTE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LivroDisponivel getLivroDisponivel() {
        return livroDisponivel;
    }

    public void setLivroDisponivel(LivroDisponivel livroDisponivel) {
        this.livroDisponivel = livroDisponivel;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    // Ex: #8 Lucas Amado pediu "Duna" de João Silva - PENDENTE
    @Override
    public String toString() {
        return "#" + id + " " + solicitante.getNome() + " pediu \"" + livroDisponivel.getLivro().getTitulo()
                + "\" de " + livroDisponivel.getDono().getNome() + " - " + status;
    }
}
