package com.bookswipe.service;

import com.bookswipe.dao.ComentarioDAO;
import com.bookswipe.dao.CurtidaDAO;
import com.bookswipe.dao.EstanteDAO;
import com.bookswipe.dao.ExperienciaDAO;
import com.bookswipe.dao.LivroDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Comentario;
import com.bookswipe.model.Curtida;
import com.bookswipe.model.Experiencia;
import com.bookswipe.model.ItemEstante;
import com.bookswipe.model.Livro;
import com.bookswipe.model.StatusLeitura;
import com.bookswipe.model.TipoCurtida;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * A rede social: publicar experiências, swipe (❤/❌) e comentários.
 */
public class SocialService {

    private final ExperienciaDAO experienciaDAO = new ExperienciaDAO();
    private final CurtidaDAO curtidaDAO = new CurtidaDAO();
    private final ComentarioDAO comentarioDAO = new ComentarioDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LivroDAO livroDAO = new LivroDAO();
    private final EstanteDAO estanteDAO = new EstanteDAO();

    public List<Experiencia> descobrir(int usuarioId) throws SQLException {
        return experienciaDAO.listarParaDescobrir(usuarioId);
    }

    public List<Experiencia> experienciasDoLivro(int livroId) throws SQLException {
        return experienciaDAO.listarPorLivro(livroId);
    }

    public List<Experiencia> experienciasDoUsuario(int usuarioId) throws SQLException {
        return experienciaDAO.listarPorUsuario(usuarioId);
    }

    public List<Comentario> comentarios(int experienciaId) throws SQLException {
        return comentarioDAO.listarPorExperiencia(experienciaId);
    }

    // "Publicar experiência". Regra: uma experiência por livro por pessoa.
    // Quem publica sobre um livro já leu: o livro vai para a estante como LIDO.
    public Experiencia publicar(int usuarioId, int livroId, int nota, String sentimento,
                                String texto, String trecho) throws SQLException {
        Usuario autor = usuarioDAO.buscarPorId(usuarioId);
        Livro livro = livroDAO.buscarPorId(livroId);
        if (autor == null || livro == null) {
            throw new RegraNegocioException("Usuário ou livro não encontrado");
        }
        boolean jaPublicou = experienciaDAO.listarPorUsuario(usuarioId).stream()
                .anyMatch(e -> e.getLivro().getId() == livroId);
        if (jaPublicou) {
            throw new RegraNegocioException("Você já publicou uma experiência sobre este livro");
        }

        Experiencia exp = new Experiencia(autor, livro, nota, sentimento, texto, trecho);
        experienciaDAO.inserir(exp);

        ItemEstante naEstante = estanteDAO.buscar(usuarioId, livroId);
        boolean favorito = naEstante != null && naEstante.isFavorito(); // não perde o favorito
        estanteDAO.salvar(new ItemEstante(usuarioId, livro, StatusLeitura.LIDO, favorito));
        return exp;
    }

    // O swipe. Regra: não pode avaliar o próprio post.
    public void avaliar(int usuarioId, int experienciaId, TipoCurtida tipo) throws SQLException {
        Experiencia exp = experienciaDAO.buscarPorId(experienciaId);
        if (exp == null) {
            throw new RegraNegocioException("Experiência não encontrada");
        }
        if (exp.getUsuario().getId() == usuarioId) {
            throw new RegraNegocioException("Você não pode avaliar a sua própria experiência");
        }
        curtidaDAO.registrar(new Curtida(usuarioId, experienciaId, tipo));
    }

    public Comentario comentar(int usuarioId, int experienciaId, String texto) throws SQLException {
        Usuario autor = usuarioDAO.buscarPorId(usuarioId);
        if (autor == null || experienciaDAO.buscarPorId(experienciaId) == null) {
            throw new RegraNegocioException("Usuário ou experiência não encontrado");
        }
        Comentario comentario = new Comentario(autor, experienciaId, texto); // valida o texto
        comentarioDAO.inserir(comentario);
        return comentario;
    }
}
