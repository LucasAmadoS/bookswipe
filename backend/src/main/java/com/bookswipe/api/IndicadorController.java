package com.bookswipe.api;

import com.bookswipe.service.IndicadorService;
import io.javalin.Javalin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rotas dos indicadores (dashboard "Meu impacto" e landing page "1.284 livros já reutilizados").
 */
public class IndicadorController {

    private static final IndicadorService indicadores = new IndicadorService();

    public static void registrar(Javalin app) {

        // Tudo da comunidade numa chamada só, para o dashboard
        app.get("/api/indicadores", ctx -> {
            Map<String, Object> painel = new LinkedHashMap<>(); // mantém a ordem das chaves no JSON
            painel.put("livrosReutilizados", indicadores.livrosReutilizados());
            painel.put("taxaReuso", indicadores.taxaReuso());
            painel.put("engajamento", indicadores.taxaEngajamento());
            painel.put("trocasPorMes", indicadores.trocasPorMes());
            painel.put("ranking", indicadores.rankingComunidade(3));
            painel.put("emAlta", indicadores.emAlta(5));
            painel.put("generosMaisLidos", indicadores.generosMaisLidos());
            ctx.json(painel);
        });

        // Números do perfil: {"livrosLidos":3,"favoritos":2,"livrosReutilizados":1}
        app.get("/api/usuarios/{id}/impacto",
                ctx -> ctx.json(indicadores.impactoDoUsuario(Autenticacao.dono(ctx))));
    }
}
