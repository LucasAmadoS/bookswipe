package com.bookswipe.externo;

import com.bookswipe.model.SugestaoLivro;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

/**
 * O "contrato" de qualquer API pública de livros.
 *
 * POLIMORFISMO: o resto do sistema só conhece esta interface. Hoje quem responde é a
 * OpenLibraryBuscador; com uma chave configurada, passa a ser a GoogleBooksBuscador.
 * Nenhuma outra classe precisa mudar para trocar de fonte.
 */
public interface BuscadorDeLivros {

    // Nome da fonte, para mostrar na tela ("Open Library")
    String nome();

    // Pesquisa livre: "hobbit tolkien"
    List<SugestaoLivro> buscar(String texto) throws IOException;

    // Busca mais detalhes de um resultado (ex: a sinopse). Por padrão, devolve como veio.
    default SugestaoLivro detalhar(SugestaoLivro sugestao) throws IOException {
        return sugestao;
    }

    // Texto de busca para achar um livro que já temos (cada API pode montar do seu jeito)
    default String consultaPara(String titulo, String autor) {
        return titulo + " " + autor;
    }

    /**
     * Acha o livro certo para um título + autor que já temos no banco:
     * o primeiro resultado que tem capa e cujo autor bate com o nosso.
     */
    default Optional<SugestaoLivro> encontrar(String titulo, String autor) throws IOException {
        String sobrenome = normalizar(autor).replaceAll(".*\\s", ""); // "J.R.R. Tolkien" -> "tolkien"
        for (SugestaoLivro s : buscar(consultaPara(titulo, autor))) {
            boolean autorBate = s.autor() != null && normalizar(s.autor()).contains(sobrenome);
            if (s.capaUrl() != null && autorBate) {
                return Optional.of(detalhar(s));
            }
        }
        return Optional.empty();
    }

    // "Código" -> "codigo": tira acentos e deixa minúsculo, para comparar textos
    static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().trim();
    }
}
