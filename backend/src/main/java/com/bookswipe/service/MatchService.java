package com.bookswipe.service;

import com.bookswipe.dao.LivroDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.dao.UsuarioGeneroDAO;
import com.bookswipe.model.Genero;
import com.bookswipe.model.Livro;
import com.bookswipe.model.Match;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calcula a afinidade literária entre leitores (tela "Matches").
 *
 * Para cada pessoa:
 *   livros de interesse = livros que leu + livros dos posts em que deu ❤
 *   gêneros            = gêneros favoritos do cadastro
 *
 * Semelhança de Jaccard = em comum / todos juntos (0 a 1).
 *   Ex: eu {Hobbit, 1984}, Ana {Hobbit, Duna} -> em comum 1, juntos 3 -> 0,33
 *
 * Afinidade = 60% semelhança de livros + 40% semelhança de gêneros.
 * Livros pesam mais porque mostram o que a pessoa leu de verdade.
 */
public class MatchService {

    private static final double PESO_LIVROS = 0.6;
    private static final double PESO_GENEROS = 0.4;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LivroDAO livroDAO = new LivroDAO();
    private final UsuarioGeneroDAO usuarioGeneroDAO = new UsuarioGeneroDAO();

    // Todos os matches de um usuário, do mais parecido para o menos. Afinidade 0 não aparece.
    public List<Match> buscarMatches(int usuarioId) throws SQLException {
        List<Livro> meusLivros = livroDAO.listarDeInteresse(usuarioId);
        List<Genero> meusGeneros = usuarioGeneroDAO.listarDoUsuario(usuarioId);

        List<Match> matches = new ArrayList<>();
        for (Usuario outro : usuarioDAO.listarTodos()) {
            if (outro.getId() == usuarioId) {
                continue; // não dá match consigo mesmo
            }
            List<Livro> livrosDele = livroDAO.listarDeInteresse(outro.getId());
            List<Genero> generosDele = usuarioGeneroDAO.listarDoUsuario(outro.getId());

            double semelhancaLivros = jaccard(idsDosLivros(meusLivros), idsDosLivros(livrosDele));
            double semelhancaGeneros = jaccard(idsDosGeneros(meusGeneros), idsDosGeneros(generosDele));
            int afinidade = (int) Math.round((PESO_LIVROS * semelhancaLivros + PESO_GENEROS * semelhancaGeneros) * 100);

            if (afinidade > 0) {
                List<Livro> livrosEmComum = livrosDele.stream()
                        .filter(l -> idsDosLivros(meusLivros).contains(l.getId()))
                        .toList();
                List<Genero> generosEmComum = generosDele.stream()
                        .filter(g -> idsDosGeneros(meusGeneros).contains(g.getId()))
                        .toList();
                matches.add(new Match(perfilPublico(outro), afinidade, livrosEmComum, generosEmComum));
            }
        }

        matches.sort(Comparator.comparingInt(Match::getAfinidade).reversed());
        return matches;
    }

    // Privacidade: no match só aparece o que é público (sem e-mail e sem senha)
    private Usuario perfilPublico(Usuario completo) {
        Usuario publico = new Usuario();
        publico.setId(completo.getId());
        publico.setNome(completo.getNome());
        publico.setCurso(completo.getCurso());
        publico.setBairro(completo.getBairro());
        return publico;
    }

    // Jaccard: tamanho da interseção / tamanho da união
    private double jaccard(Set<Integer> a, Set<Integer> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0;
        }
        Set<Integer> intersecao = new HashSet<>(a);
        intersecao.retainAll(b); // fica só o que está nos dois

        Set<Integer> uniao = new HashSet<>(a);
        uniao.addAll(b);         // junta tudo, sem repetir

        return (double) intersecao.size() / uniao.size();
    }

    private Set<Integer> idsDosLivros(List<Livro> livros) {
        Set<Integer> ids = new HashSet<>();
        for (Livro l : livros) {
            ids.add(l.getId());
        }
        return ids;
    }

    private Set<Integer> idsDosGeneros(List<Genero> generos) {
        Set<Integer> ids = new HashSet<>();
        for (Genero g : generos) {
            ids.add(g.getId());
        }
        return ids;
    }
}
