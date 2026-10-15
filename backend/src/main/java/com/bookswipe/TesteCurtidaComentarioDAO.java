package com.bookswipe;

import com.bookswipe.dao.ComentarioDAO;
import com.bookswipe.dao.CurtidaDAO;
import com.bookswipe.dao.ExperienciaDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Comentario;
import com.bookswipe.model.Curtida;
import com.bookswipe.model.TipoCurtida;
import com.bookswipe.model.Usuario;

import java.sql.SQLException;

/**
 * Testa o swipe e os comentários.
 * O Lucas avalia o post do João sobre Sapiens (id 8), comenta e depois tudo é desfeito.
 */
public class TesteCurtidaComentarioDAO {

    public static void main(String[] args) {
        CurtidaDAO curtidaDAO = new CurtidaDAO();
        ComentarioDAO comentarioDAO = new ComentarioDAO();
        ExperienciaDAO experienciaDAO = new ExperienciaDAO();

        int postSapiens = 8;

        try {
            Usuario lucas = new UsuarioDAO().buscarPorId(1);

            System.out.println("1) Posts na tela Descobrir do Lucas: "
                    + experienciaDAO.listarParaDescobrir(lucas.getId()).size());

            // Swipe ❤
            curtidaDAO.registrar(new Curtida(lucas.getId(), postSapiens, TipoCurtida.GOSTEI));
            System.out.println("2) Lucas deu ❤. Voto salvo: " + curtidaDAO.buscar(lucas.getId(), postSapiens));
            System.out.println("   ❤ no post agora: " + curtidaDAO.contar(postSapiens, TipoCurtida.GOSTEI));
            System.out.println("   Posts no Descobrir agora: "
                    + experienciaDAO.listarParaDescobrir(lucas.getId()).size() + " (o post saiu da fila)");

            // Mudou de ideia: ❌ (não cria outra linha, só troca o tipo)
            curtidaDAO.registrar(new Curtida(lucas.getId(), postSapiens, TipoCurtida.PASSAR));
            System.out.println("3) Mudou para ❌. Voto salvo: " + curtidaDAO.buscar(lucas.getId(), postSapiens));
            System.out.println("   ❤ no post agora: " + curtidaDAO.contar(postSapiens, TipoCurtida.GOSTEI));

            // Comentário
            Comentario novo = new Comentario(lucas, postSapiens, "Quero ler esse! Você empresta?");
            comentarioDAO.inserir(novo);
            System.out.println("4) Comentários do post:");
            for (Comentario c : comentarioDAO.listarPorExperiencia(postSapiens)) {
                System.out.println("   " + c);
            }

            // Validação: comentário vazio
            try {
                new Comentario(lucas, postSapiens, "   ");
            } catch (IllegalArgumentException erro) {
                System.out.println("5) Comentário vazio -> bloqueado: " + erro.getMessage());
            }

            // Desfaz tudo
            comentarioDAO.excluir(novo.getId());
            curtidaDAO.remover(lucas.getId(), postSapiens);
            System.out.println("6) Desfeito. Voto: " + curtidaDAO.buscar(lucas.getId(), postSapiens)
                    + " | posts no Descobrir: " + experienciaDAO.listarParaDescobrir(lucas.getId()).size());

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
