package com.bookswipe.api;

import com.bookswipe.service.RegraNegocioException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;

import java.sql.SQLException;
import java.util.Map;

/**
 * Liga a API REST do BookSwipe. Rode ESTA classe e deixe rodando enquanto usa o site.
 * Teste no navegador: http://localhost:7070/api
 *
 * O site (HTML/CSS/JS) chama estas rotas com fetch() e recebe JSON.
 */
public class ApiServidor {

    public static void main(String[] args) {
        // Porta padrão 7070; dá para trocar com a variável de ambiente PORTA (ex: para testes)
        final int PORTA = Integer.parseInt(System.getenv().getOrDefault("PORTA", "7070"));

        Javalin app = Javalin.create(config -> {
            // Jackson: datas viram texto "2026-06-01T20:10:00" em vez de uma lista de números
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                // Campos vazios (null) não aparecem no JSON: resposta mais limpa e sem "email": null
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }));
            // CORS: deixa o site aberto pelo Live Server (outra porta) chamar a API
            config.bundledPlugins.enableCors(cors -> cors.addRule(regra -> regra.anyHost()));
        });

        // Página de boas-vindas para conferir se a API está no ar
        app.get("/api", ctx -> ctx.json(Map.of(
                "status", "BookSwipe API no ar 📖♻️",
                "exemplos", new String[]{"/api/livros", "/api/indicadores", "/api/usuarios/1/matches"})));

        // O porteiro: confere o token antes de QUALQUER rota /api/... (menos as públicas)
        app.before("/api/*", Autenticacao::verificar);

        UsuarioController.registrar(app);
        LivroController.registrar(app);
        SocialController.registrar(app);
        TrocaController.registrar(app);
        IndicadorController.registrar(app);

        // ---------- Erros viram respostas JSON {"erro": "..."} com o código HTTP certo ----------

        // Regra quebrada (ex: pedir o próprio livro) -> 400 Bad Request
        app.exception(RegraNegocioException.class, (e, ctx) ->
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("erro", e.getMessage())));

        // Validação do model (ex: nota 7, comentário vazio) -> 400
        app.exception(IllegalArgumentException.class, (e, ctx) ->
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("erro", e.getMessage())));

        // JSON mal escrito pelo site -> 400
        app.exception(JacksonException.class, (e, ctx) ->
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("erro", "JSON inválido: confira os campos enviados")));

        // 404 (não encontrado), 400 de parâmetro etc. que nós mesmos lançamos
        app.exception(HttpResponseException.class, (e, ctx) ->
                ctx.status(e.getStatus()).json(Map.of("erro", e.getMessage())));

        // Problema no banco -> 500 (não mostra detalhes técnicos para o site)
        app.exception(SQLException.class, (e, ctx) -> {
            System.err.println("Erro no banco: " + e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("erro", "Erro no banco de dados"));
        });

        app.start(PORTA);
        System.out.println("\n📖 BookSwipe API rodando em http://localhost:" + PORTA + "/api");
    }
}
