package com.bookswipe.api;

import com.bookswipe.model.CondicaoLivro;
import com.bookswipe.model.StatusLeitura;
import com.bookswipe.model.TipoCurtida;

import java.util.List;

/**
 * O formato do JSON que o site MANDA para a API em cada POST/PUT.
 * Cada record vira um objeto Java automático: o Jackson lê o JSON e preenche os campos pelo nome.
 *
 * Repare: nenhum record tem "usuarioId". QUEM está fazendo a ação vem do token de sessão,
 * nunca do JSON — assim ninguém consegue agir no nome de outra pessoa.
 */
public class Requisicoes {

    public record Login(String email, String senha) {
    }

    public record Cadastro(String nome, String email, String senha, String curso, String bairro) {
    }

    public record Generos(List<Integer> generoIds) {
    }

    public record NovoLivro(String titulo, String autor, int generoId) {
    }

    // Livro escolhido da busca na internet + o gênero que o usuário escolheu
    public record Importacao(String titulo, String autor, String isbn, String sinopse,
                             String capaUrl, String chaveExterna, int generoId) {
    }

    public record NovaExperiencia(int livroId, int nota, String sentimento, String texto, String trechoFavorito) {
    }

    public record Avaliacao(TipoCurtida tipo) {
    }

    public record NovoComentario(String texto) {
    }

    public record MudarEstante(StatusLeitura status) {
    }

    public record Oferta(int livroId, CondicaoLivro condicao) {
    }

    public record Pedido(int livroDisponivelId) {
    }
}
