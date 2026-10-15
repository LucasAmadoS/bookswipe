package com.bookswipe;

import com.bookswipe.dao.EstanteDAO;
import com.bookswipe.dao.LivroDAO;
import com.bookswipe.dao.UsuarioGeneroDAO;
import com.bookswipe.model.Genero;
import com.bookswipe.model.ItemEstante;
import com.bookswipe.model.Livro;
import com.bookswipe.model.StatusLeitura;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Testa a "Minha estante" e os gêneros favoritos do Lucas (id 1).
 * No final tudo volta como estava.
 */
public class TesteEstanteGeneroDAO {

    public static void main(String[] args) {
        EstanteDAO estanteDAO = new EstanteDAO();
        UsuarioGeneroDAO generoFavDAO = new UsuarioGeneroDAO();
        int lucas = 1;

        try {
            System.out.println("1) Estante do Lucas:");
            for (ItemEstante item : estanteDAO.listar(lucas)) {
                System.out.println("   " + item);
            }
            System.out.println("   Totais (GROUP BY): " + estanteDAO.contarPorStatus(lucas));

            // Lucas começa a ler Jogos Vorazes
            Livro jogosVorazes = new LivroDAO().buscarPorId(4);
            estanteDAO.salvar(new ItemEstante(lucas, jogosVorazes, StatusLeitura.LENDO, false));
            System.out.println("2) Aba Lendo: " + estanteDAO.listarPorStatus(lucas, StatusLeitura.LENDO));

            // Terminou e favoritou (mesma linha, só muda status e favorito)
            estanteDAO.salvar(new ItemEstante(lucas, jogosVorazes, StatusLeitura.LIDO, true));
            System.out.println("3) Aba Favoritos: " + estanteDAO.listarFavoritos(lucas));
            System.out.println("   Totais agora: " + estanteDAO.contarPorStatus(lucas));

            // Gêneros favoritos
            List<Genero> antes = generoFavDAO.listarDoUsuario(lucas);
            System.out.println("4) Gêneros do Lucas: " + antes);

            generoFavDAO.definir(lucas, List.of(1, 3)); // Fantasia e Terror
            System.out.println("5) Depois de remarcar: " + generoFavDAO.listarDoUsuario(lucas));

            // Volta tudo como estava
            estanteDAO.remover(lucas, jogosVorazes.getId());
            generoFavDAO.definir(lucas, antes.stream().map(Genero::getId).toList());
            Map<StatusLeitura, Integer> totais = estanteDAO.contarPorStatus(lucas);
            System.out.println("6) Restaurado. Totais: " + totais
                    + " | gêneros: " + generoFavDAO.listarDoUsuario(lucas));

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
