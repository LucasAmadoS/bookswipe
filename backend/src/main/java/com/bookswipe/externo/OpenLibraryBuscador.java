package com.bookswipe.externo;

import com.bookswipe.model.SugestaoLivro;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Open Library (openlibrary.org): API pública e gratuita, sem chave.
 *
 * Busca:   https://openlibrary.org/search.json?q=hobbit
 * Sinopse: https://openlibrary.org/works/OL27482W.json  (campo "description")
 * Capa:    https://covers.openlibrary.org/b/id/{cover_i}-L.jpg
 */
public class OpenLibraryBuscador implements BuscadorDeLivros {

    private static final String BUSCA = "https://openlibrary.org/search.json?limit=8"
            + "&fields=key,title,author_name,isbn,cover_i&q=";

    @Override
    public String nome() {
        return "Open Library";
    }

    @Override
    public List<SugestaoLivro> buscar(String texto) throws IOException {
        JsonNode resposta = ClienteHttp.getJson(BUSCA + ClienteHttp.codificar(texto));
        List<SugestaoLivro> sugestoes = new ArrayList<>();

        for (JsonNode doc : resposta.path("docs")) {
            String capa = doc.has("cover_i")
                    ? "https://covers.openlibrary.org/b/id/" + doc.get("cover_i").asText() + "-L.jpg"
                    : null;
            sugestoes.add(new SugestaoLivro(
                    ClienteHttp.texto(doc, "title"),
                    doc.path("author_name").path(0).asText(null),
                    escolherIsbn(doc.path("isbn")),
                    null, // a busca não traz sinopse; vem no detalhar()
                    capa,
                    ClienteHttp.texto(doc, "key"),
                    nome()));
        }
        return sugestoes;
    }

    // A sinopse fica em outra URL: /works/OL...W.json
    @Override
    public SugestaoLivro detalhar(SugestaoLivro sugestao) throws IOException {
        if (sugestao.chaveExterna() == null || sugestao.sinopse() != null) {
            return sugestao;
        }
        JsonNode obra = ClienteHttp.getJson("https://openlibrary.org" + sugestao.chaveExterna() + ".json");
        JsonNode descricao = obra.path("description");
        // Às vezes vem como texto, às vezes como {"value": "texto"}
        String sinopse = descricao.isTextual() ? descricao.asText() : descricao.path("value").asText(null);
        return sugestao.comSinopse(sinopse);
    }

    // A Open Library acha melhor sem acento e sem artigo: "O Código Da Vinci" -> "codigo da vinci brown"
    @Override
    public String consultaPara(String titulo, String autor) {
        String semArtigo = BuscadorDeLivros.normalizar(titulo).replaceFirst("^(o|a|os|as) ", "");
        String sobrenome = BuscadorDeLivros.normalizar(autor).replaceAll(".*\\s", "");
        return semArtigo + " " + sobrenome;
    }

    // Prefere o ISBN de 13 dígitos (o atual)
    private String escolherIsbn(JsonNode isbns) {
        String primeiro = null;
        for (JsonNode isbn : isbns) {
            if (isbn.asText().length() == 13) {
                return isbn.asText();
            }
            if (primeiro == null) {
                primeiro = isbn.asText();
            }
        }
        return primeiro;
    }
}
