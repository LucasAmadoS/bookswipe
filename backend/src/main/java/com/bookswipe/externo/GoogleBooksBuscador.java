package com.bookswipe.externo;

import com.bookswipe.model.SugestaoLivro;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Books (googleapis.com/books). Precisa de uma chave de API gratuita:
 * sem chave, a cota pública costuma estar esgotada (erro 429).
 * A chave fica em src/main/resources/apis.properties (google.books.chave=...).
 *
 * Busca: https://www.googleapis.com/books/v1/volumes?q=intitle:hobbit+inauthor:tolkien&key=CHAVE
 * Já traz capa, ISBN e sinopse (muitas vezes em português) numa chamada só.
 */
public class GoogleBooksBuscador implements BuscadorDeLivros {

    private final String chave;

    public GoogleBooksBuscador(String chave) {
        this.chave = chave;
    }

    @Override
    public String nome() {
        return "Google Books";
    }

    @Override
    public List<SugestaoLivro> buscar(String texto) throws IOException {
        String url = "https://www.googleapis.com/books/v1/volumes?maxResults=8&printType=books"
                + "&q=" + ClienteHttp.codificar(texto) + "&key=" + chave;
        JsonNode resposta = ClienteHttp.getJson(url);
        List<SugestaoLivro> sugestoes = new ArrayList<>();

        for (JsonNode item : resposta.path("items")) {
            JsonNode info = item.path("volumeInfo");
            String capa = info.path("imageLinks").path("thumbnail").asText(null);
            if (capa != null) {
                capa = capa.replace("http://", "https://").replace("&edge=curl", ""); // https e sem "dobra" na capa
            }
            sugestoes.add(new SugestaoLivro(
                    ClienteHttp.texto(info, "title"),
                    info.path("authors").path(0).asText(null),
                    escolherIsbn(info.path("industryIdentifiers")),
                    ClienteHttp.texto(info, "description"),
                    capa,
                    ClienteHttp.texto(item, "id"),
                    nome()));
        }
        return sugestoes;
    }

    // A Google entende filtros: intitle (no título) e inauthor (no autor)
    @Override
    public String consultaPara(String titulo, String autor) {
        return "intitle:" + titulo + " inauthor:" + autor;
    }

    private String escolherIsbn(JsonNode identificadores) {
        String isbn10 = null;
        for (JsonNode id : identificadores) {
            if ("ISBN_13".equals(id.path("type").asText())) {
                return id.path("identifier").asText();
            }
            if ("ISBN_10".equals(id.path("type").asText())) {
                isbn10 = id.path("identifier").asText();
            }
        }
        return isbn10;
    }
}
