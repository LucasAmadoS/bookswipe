package com.bookswipe.console;

import com.bookswipe.service.RegraNegocioException;

import java.sql.SQLException;

/**
 * Formatação da tela e tratamento de erros em um lugar só.
 */
public class Tela {

    private static final String LINHA_DUPLA = "═".repeat(56);
    private static final String LINHA = "─".repeat(56);

    public static void titulo(String texto) {
        System.out.println();
        System.out.println(LINHA_DUPLA);
        System.out.println("  " + texto);
        System.out.println(LINHA_DUPLA);
    }

    public static void secao(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println(LINHA);
    }

    public static void ok(String mensagem) {
        System.out.println("✓ " + mensagem);
    }

    public static void aviso(String mensagem) {
        System.out.println("⚠ " + mensagem);
    }

    // 4 -> ★★★★☆
    public static String estrelas(int nota) {
        return "★".repeat(nota) + "☆".repeat(5 - nota);
    }

    // Barra de gráfico: 3 -> ███
    public static String barra(int valor, int escala) {
        return "█".repeat(Math.max(1, valor * escala));
    }

    /**
     * Uma ação do menu que pode dar erro de banco.
     * É uma "interface funcional": cabe numa lambda, ex: () -> mostrarEstante(usuario)
     */
    public interface Acao {
        void executar() throws SQLException;
    }

    // Roda a ação e, se der erro, mostra a mensagem e volta para o menu (o programa não fecha)
    public static void executar(Acao acao) {
        try {
            acao.executar();
        } catch (RegraNegocioException | IllegalArgumentException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            aviso("Erro no banco de dados: " + e.getMessage());
        }
    }
}
