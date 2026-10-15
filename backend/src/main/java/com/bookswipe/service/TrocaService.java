package com.bookswipe.service;

import com.bookswipe.dao.LivroDAO;
import com.bookswipe.dao.LivroDisponivelDAO;
import com.bookswipe.dao.SolicitacaoTrocaDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.CondicaoLivro;
import com.bookswipe.model.Livro;
import com.bookswipe.model.LivroDisponivel;
import com.bookswipe.model.SolicitacaoTroca;
import com.bookswipe.model.StatusDisponivel;
import com.bookswipe.model.StatusSolicitacao;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Regras da troca de livros. O console e a API chamam ESTA classe, nunca o DAO direto,
 * para que as regras valham nos dois lugares.
 *
 * Regras:
 *  1. Só se pede livro que está DISPONIVEL.
 *  2. Ninguém pede o próprio livro.
 *  3. Não pode ter dois pedidos abertos do mesmo livro pela mesma pessoa.
 *  4. Só o dono aceita, recusa ou conclui.
 */
public class TrocaService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LivroDAO livroDAO = new LivroDAO();
    private final LivroDisponivelDAO disponivelDAO = new LivroDisponivelDAO();
    private final SolicitacaoTrocaDAO solicitacaoDAO = new SolicitacaoTrocaDAO();

    // ---------- Consultas (o console e a API leem por aqui) ----------

    public List<LivroDisponivel> marketplace(int usuarioId, int generoId) throws SQLException {
        return disponivelDAO.listarMarketplace(usuarioId, generoId);
    }

    public List<LivroDisponivel> meusOferecidos(int usuarioId) throws SQLException {
        return disponivelDAO.listarDoUsuario(usuarioId);
    }

    public List<SolicitacaoTroca> pedidosRecebidos(int usuarioId) throws SQLException {
        return solicitacaoDAO.listarRecebidas(usuarioId);
    }

    public List<SolicitacaoTroca> meusPedidos(int usuarioId) throws SQLException {
        return solicitacaoDAO.listarFeitas(usuarioId);
    }

    // ---------- Ações ----------

    // "Disponibilizar para troca"
    public LivroDisponivel oferecer(int usuarioId, int livroId, CondicaoLivro condicao) throws SQLException {
        Usuario dono = usuarioDAO.buscarPorId(usuarioId);
        Livro livro = livroDAO.buscarPorId(livroId);
        if (dono == null || livro == null) {
            throw new RegraNegocioException("Usuário ou livro não encontrado");
        }
        LivroDisponivel ld = new LivroDisponivel(dono, livro, condicao);
        disponivelDAO.inserir(ld);
        return ld;
    }

    // "Solicitar troca"
    public SolicitacaoTroca solicitar(int solicitanteId, int livroDisponivelId) throws SQLException {
        LivroDisponivel ld = disponivelDAO.buscarPorId(livroDisponivelId);
        Usuario solicitante = usuarioDAO.buscarPorId(solicitanteId);
        if (ld == null || solicitante == null) {
            throw new RegraNegocioException("Livro ou usuário não encontrado");
        }
        if (ld.getStatus() != StatusDisponivel.DISPONIVEL) {                       // regra 1
            throw new RegraNegocioException("Este livro não está mais disponível (" + ld.getStatus() + ")");
        }
        if (ld.getDono().getId() == solicitanteId) {                                // regra 2
            throw new RegraNegocioException("Você não pode pedir o seu próprio livro");
        }
        boolean jaPediu = solicitacaoDAO.listarFeitas(solicitanteId).stream()       // regra 3
                .anyMatch(s -> s.getLivroDisponivel().getId() == livroDisponivelId
                        && (s.getStatus() == StatusSolicitacao.PENDENTE || s.getStatus() == StatusSolicitacao.ACEITA));
        if (jaPediu) {
            throw new RegraNegocioException("Você já tem um pedido aberto para este livro");
        }

        SolicitacaoTroca sol = new SolicitacaoTroca(ld, solicitante);
        solicitacaoDAO.inserir(sol);
        return sol;
    }

    public void aceitar(int donoId, int solicitacaoId) throws SQLException {
        verificarDono(donoId, solicitacaoId);                                       // regra 4
        if (!solicitacaoDAO.aceitar(solicitacaoId)) {
            throw new RegraNegocioException("Só dá para aceitar um pedido PENDENTE");
        }
    }

    public void recusar(int donoId, int solicitacaoId) throws SQLException {
        verificarDono(donoId, solicitacaoId);
        if (!solicitacaoDAO.recusar(solicitacaoId)) {
            throw new RegraNegocioException("Só dá para recusar um pedido PENDENTE");
        }
    }

    // O dono confirma que entregou o livro: +1 livro reutilizado ♻️
    public void concluir(int donoId, int solicitacaoId) throws SQLException {
        verificarDono(donoId, solicitacaoId);
        if (!solicitacaoDAO.concluir(solicitacaoId)) {
            throw new RegraNegocioException("Só dá para concluir um pedido ACEITO");
        }
    }

    private void verificarDono(int donoId, int solicitacaoId) throws SQLException {
        SolicitacaoTroca sol = solicitacaoDAO.buscarPorId(solicitacaoId);
        if (sol == null) {
            throw new RegraNegocioException("Pedido não encontrado");
        }
        if (sol.getLivroDisponivel().getDono().getId() != donoId) {
            throw new RegraNegocioException("Só o dono do livro pode responder este pedido");
        }
    }
}
