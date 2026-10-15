package com.bookswipe;

import com.bookswipe.model.Contagem;
import com.bookswipe.model.Match;
import com.bookswipe.service.IndicadorService;
import com.bookswipe.service.MatchService;
import com.bookswipe.service.RegraNegocioException;
import com.bookswipe.service.TrocaService;

import java.sql.SQLException;

/**
 * Testa os três services. Não altera o banco: as regras de troca testadas aqui
 * são todas tentativas que devem ser BLOQUEADAS.
 */
public class TesteServices {

    public static void main(String[] args) {
        int lucas = 1;

        try {
            // ---------- MATCH ----------
            System.out.println("1) Matches do Lucas:");
            for (Match m : new MatchService().buscarMatches(lucas)) {
                System.out.println("   " + m);
            }

            // ---------- INDICADORES ----------
            IndicadorService ind = new IndicadorService();
            System.out.println("2) Indicadores de sustentabilidade:");
            System.out.println("   ♻️ Livros reutilizados: " + ind.livrosReutilizados());
            System.out.println("   Taxa de reuso: " + ind.taxaReuso() + "%");
            System.out.println("   Engajamento: " + ind.taxaEngajamento() + " interações por post");
            System.out.println("   Trocas por mês:");
            for (Contagem c : ind.trocasPorMes()) {
                System.out.println("     " + c.rotulo() + "  " + "█".repeat(c.total() * 4) + " " + c.total());
            }
            System.out.println("   🏆 Ranking: " + ind.rankingComunidade(3));
            System.out.println("   🔥 Em alta: " + ind.emAlta(3));
            System.out.println("   Gêneros mais lidos: " + ind.generosMaisLidos());
            System.out.println("   Impacto do Lucas: " + ind.impactoDoUsuario(lucas));

            // ---------- REGRAS DA TROCA (todas devem ser bloqueadas) ----------
            TrocaService troca = new TrocaService();
            System.out.println("3) Regras da troca:");
            tentar("Lucas pede o próprio 1984 (exemplar 2)", () -> troca.solicitar(lucas, 2));
            tentar("Lucas pede de novo Duna (já tem pedido #8)", () -> troca.solicitar(lucas, 9));
            tentar("Lucas pede O Hobbit já trocado (exemplar 1)", () -> troca.solicitar(lucas, 1));
            tentar("Lucas tenta aceitar pedido do livro do João (#8)", () -> troca.aceitar(lucas, 8));
            tentar("João conclui pedido #8 que ainda está PENDENTE", () -> troca.concluir(3, 8));

        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }

    // Executa uma ação que deve ser bloqueada e mostra a mensagem da regra
    interface Acao {
        void executar() throws SQLException;
    }

    private static void tentar(String descricao, Acao acao) throws SQLException {
        try {
            acao.executar();
            System.out.println("   ✗ " + descricao + " -> PASSOU (não devia!)");
        } catch (RegraNegocioException e) {
            System.out.println("   ✓ " + descricao + " -> bloqueado: " + e.getMessage());
        }
    }
}
