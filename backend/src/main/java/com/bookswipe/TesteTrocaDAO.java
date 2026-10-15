package com.bookswipe;

import com.bookswipe.dao.LivroDAO;
import com.bookswipe.dao.LivroDisponivelDAO;
import com.bookswipe.dao.SolicitacaoTrocaDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.CondicaoLivro;
import com.bookswipe.model.LivroDisponivel;
import com.bookswipe.model.SolicitacaoTroca;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;

/**
 * Testa o fluxo completo de uma troca:
 * Eliane oferece O Hobbit -> João e Mariana pedem -> Eliane aceita o João -> troca concluída.
 * No final tudo é apagado e o banco volta como estava.
 */
public class TesteTrocaDAO {

    public static void main(String[] args) {
        LivroDisponivelDAO disponivelDAO = new LivroDisponivelDAO();
        SolicitacaoTrocaDAO solicitacaoDAO = new SolicitacaoTrocaDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try {
            Usuario eliane = usuarioDAO.buscarPorId(4);
            Usuario joao = usuarioDAO.buscarPorId(3);
            Usuario mariana = usuarioDAO.buscarPorId(5);

            System.out.println("0) Livros reutilizados até agora: " + solicitacaoDAO.contarConcluidas());

            System.out.println("1) Marketplace visto pelo João:");
            for (LivroDisponivel ld : disponivelDAO.listarMarketplace(joao.getId(), 0)) {
                System.out.println("   " + ld);
            }

            // Eliane oferece O Hobbit
            LivroDisponivel hobbit = new LivroDisponivel(eliane, new LivroDAO().buscarPorId(1), CondicaoLivro.BOA);
            disponivelDAO.inserir(hobbit);
            System.out.println("2) Eliane ofereceu: " + disponivelDAO.buscarPorId(hobbit.getId()));
            System.out.println("   Só Fantasia no marketplace do João: "
                    + disponivelDAO.listarMarketplace(joao.getId(), 1).size() + " livro(s)");

            // João e Mariana pedem
            SolicitacaoTroca pedidoJoao = new SolicitacaoTroca(hobbit, joao);
            SolicitacaoTroca pedidoMariana = new SolicitacaoTroca(hobbit, mariana);
            solicitacaoDAO.inserir(pedidoJoao);
            solicitacaoDAO.inserir(pedidoMariana);
            System.out.println("3) Pedidos que a Eliane recebeu:");
            for (SolicitacaoTroca s : solicitacaoDAO.listarRecebidas(eliane.getId())) {
                System.out.println("   " + s);
            }

            // Eliane aceita o João
            solicitacaoDAO.aceitar(pedidoJoao.getId());
            System.out.println("4) Eliane aceitou o João:");
            System.out.println("   " + solicitacaoDAO.buscarPorId(pedidoJoao.getId()));
            System.out.println("   " + solicitacaoDAO.buscarPorId(pedidoMariana.getId()) + "  <- recusado sozinho");
            System.out.println("   Livro: " + disponivelDAO.buscarPorId(hobbit.getId()).getStatus());

            // Tentar aceitar de novo um pedido que não está PENDENTE
            System.out.println("5) Aceitar a Mariana agora funciona? " + solicitacaoDAO.aceitar(pedidoMariana.getId()));

            // Troca feita
            solicitacaoDAO.concluir(pedidoJoao.getId());
            System.out.println("6) Troca concluída: " + solicitacaoDAO.buscarPorId(pedidoJoao.getId()));
            System.out.println("   Livro: " + disponivelDAO.buscarPorId(hobbit.getId()).getStatus());
            System.out.println("   Livros reutilizados agora: " + solicitacaoDAO.contarConcluidas() + " ♻️");

            // Volta o banco como estava (apaga o exemplar e os pedidos dele)
            disponivelDAO.excluir(hobbit.getId());
            System.out.println("7) Restaurado. Livros reutilizados: " + solicitacaoDAO.contarConcluidas());

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
