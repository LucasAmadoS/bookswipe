package com.bookswipe.externo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Faz GET em APIs de fora e devolve o JSON já lido.
 * Usa o HttpClient que vem no próprio Java (não precisa de biblioteca extra).
 */
class ClienteHttp {

    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final ObjectMapper json = new ObjectMapper();

    static JsonNode getJson(String url) throws IOException {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                // A Open Library pede que cada aplicação se identifique
                .header("User-Agent", "BookSwipe/1.0 (projeto academico UPX)")
                .timeout(Duration.ofSeconds(12))
                .GET()
                .build();
        try {
            HttpResponse<String> resposta = http.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() == 429) {
                throw new IOException("limite de consultas da API atingido, tente mais tarde");
            }
            if (resposta.statusCode() != 200) {
                throw new IOException("a API respondeu HTTP " + resposta.statusCode());
            }
            return json.readTree(resposta.body()); // texto JSON -> árvore que dá para navegar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("consulta interrompida", e);
        }
    }

    // "O Hobbit" -> "O+Hobbit" (espaços e acentos não podem ir crus na URL)
    static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }

    // Lê um campo de texto do JSON; devolve null se não existir
    static String texto(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        return (valor == null || valor.isNull()) ? null : valor.asText();
    }
}
