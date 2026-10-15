package com.bookswipe.api;

import com.bookswipe.api.Requisicoes.Cadastro;
import com.bookswipe.api.Requisicoes.Generos;
import com.bookswipe.api.Requisicoes.Login;
import com.bookswipe.model.Usuario;
import com.bookswipe.service.SessaoService;
import com.bookswipe.service.UsuarioService;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.util.Map;

/**
 * Rotas de login, logout, cadastro, perfil e gêneros.
 */
public class UsuarioController {

    private static final UsuarioService usuarios = new UsuarioService();
    private static final SessaoService sessoes = new SessaoService();

    public static void registrar(Javalin app) {

        // Login: confere a senha (BCrypt) e devolve o token + o usuário (sem senha)
        app.post("/api/login", ctx -> {
            Login dados = ctx.bodyAsClass(Login.class);
            Usuario usuario = usuarios.login(dados.email(), dados.senha());
            ctx.json(Map.of("token", sessoes.iniciar(usuario), "usuario", usuario));
        });

        // Criar conta -> 201, já logado
        app.post("/api/usuarios", ctx -> {
            Cadastro d = ctx.bodyAsClass(Cadastro.class);
            Usuario novo = usuarios.cadastrar(d.nome(), d.email(), d.senha(), d.curso(), d.bairro());
            ctx.status(HttpStatus.CREATED).json(Map.of("token", sessoes.iniciar(novo), "usuario", novo));
        });

        // Sair: apaga a sessão no banco (o token deixa de valer na hora)
        app.post("/api/logout", ctx -> {
            sessoes.encerrar(Autenticacao.token(ctx));
            ctx.status(HttpStatus.NO_CONTENT);
        });

        // Quem sou eu? (o site usa para conferir se o token ainda vale)
        app.get("/api/eu", ctx -> ctx.json(Autenticacao.usuario(ctx)));

        app.get("/api/generos", ctx -> ctx.json(usuarios.todosOsGeneros()));

        app.get("/api/usuarios/{id}/generos",
                ctx -> ctx.json(usuarios.generosDoUsuario(Autenticacao.dono(ctx))));

        // Tela "Quais gêneros você mais gosta?": substitui a lista inteira
        app.put("/api/usuarios/{id}/generos", ctx -> {
            int id = Autenticacao.dono(ctx);
            usuarios.definirGeneros(id, ctx.bodyAsClass(Generos.class).generoIds());
            ctx.json(usuarios.generosDoUsuario(id));
        });
    }
}
