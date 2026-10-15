package com.bookswipe.service;

import com.bookswipe.dao.GeneroDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.dao.UsuarioGeneroDAO;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Login, cadastro e gêneros favoritos.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final UsuarioGeneroDAO usuarioGeneroDAO = new UsuarioGeneroDAO();

    // Mensagem igual para e-mail ou senha errados: não conta para um invasor qual dos dois existe.
    // A senha digitada é comparada com o HASH salvo (o banco não tem a senha de verdade).
    public Usuario login(String email, String senha) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario == null || senha == null || !Senhas.confere(senha, usuario.getSenha())) {
            throw new RegraNegocioException("E-mail ou senha incorretos");
        }
        return usuario;
    }

    public Usuario cadastrar(String nome, String email, String senha, String curso, String bairro)
            throws SQLException {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            throw new RegraNegocioException("Nome, e-mail e senha são obrigatórios");
        }
        if (!email.contains("@")) {
            throw new RegraNegocioException("E-mail inválido");
        }
        if (senha.length() < 6) {
            throw new RegraNegocioException("A senha precisa ter pelo menos 6 caracteres");
        }
        if (usuarioDAO.buscarPorEmail(email) != null) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail");
        }
        // Grava o hash, nunca a senha
        Usuario novo = new Usuario(nome, email, Senhas.gerarHash(senha), curso, bairro);
        usuarioDAO.inserir(novo);
        return novo;
    }

    public List<Genero> todosOsGeneros() throws SQLException {
        return generoDAO.listarTodos();
    }

    public List<Genero> generosDoUsuario(int usuarioId) throws SQLException {
        return usuarioGeneroDAO.listarDoUsuario(usuarioId);
    }

    // Tela "Quais gêneros você mais gosta?" - pelo menos um, porque o match depende disso
    public void definirGeneros(int usuarioId, List<Integer> generoIds) throws SQLException {
        if (generoIds.isEmpty()) {
            throw new RegraNegocioException("Escolha pelo menos um gênero");
        }
        for (int id : generoIds) {
            if (generoDAO.buscarPorId(id) == null) {
                throw new RegraNegocioException("Gênero " + id + " não existe");
            }
        }
        usuarioGeneroDAO.definir(usuarioId, generoIds);
    }
}
