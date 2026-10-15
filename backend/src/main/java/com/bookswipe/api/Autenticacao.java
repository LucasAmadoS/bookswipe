package com.bookswipe.api;

import com.bookswipe.model.Usuario;
import com.bookswipe.service.SessaoService;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;

import java.sql.SQLException;
import java.util.Set;

/**
 * O "porteiro" da API. Roda ANTES de toda rota (app.before):
 *   1. lê o token do cabeçalho  Authorization: Bearer <token>
 *   2. descobre de quem é o token
 *   3. guarda o usuário no ctx para as rotas usarem
 * Sem token válido -> 401. Mexendo nos dados de outra pessoa -> 403.
 */
public class Autenticacao {

    private static final SessaoService sessoes = new SessaoService();

    // Rotas que funcionam sem login (verbo + caminho)
    private static final Set<String> PUBLICAS = Set.of(
            "POST /api/login",
            "POST /api/usuarios",       // criar conta
            "GET /api/generos",         // tela de cadastro
            "GET /api/indicadores");    // contador da landing page

    public static void verificar(Context ctx) throws SQLException {
        // OPTIONS é a "pergunta" do navegador sobre CORS: nunca leva token
        if (ctx.method() == HandlerType.OPTIONS || PUBLICAS.contains(ctx.method() + " " + ctx.path())) {
            return;
        }
        Usuario usuario = sessoes.usuarioDoToken(token(ctx));
        if (usuario == null) {
            throw new UnauthorizedResponse("Faça login para continuar"); // 401
        }
        ctx.attribute("usuario", usuario);
    }

    // Quem está logado (a rota não confia mais no id que o site manda)
    public static Usuario usuario(Context ctx) {
        return ctx.attribute("usuario");
    }

    // Para rotas /api/usuarios/{id}/...: o {id} tem que ser o da própria pessoa
    public static int dono(Context ctx) {
        int id = Parametros.caminho(ctx, "id");
        if (id != usuario(ctx).getId()) {
            throw new ForbiddenResponse("Você só pode acessar os seus próprios dados"); // 403
        }
        return id;
    }

    // "Authorization: Bearer kq3F9x..." -> "kq3F9x..."
    public static String token(Context ctx) {
        String cabecalho = ctx.header("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }
        return cabecalho.substring("Bearer ".length()).trim();
    }
}
