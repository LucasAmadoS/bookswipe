package com.bookswipe.console;

import java.io.Console;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Lê o que o usuário digita. Se digitar errado (letra onde é número, número fora da faixa),
 * pergunta de novo em vez de o programa quebrar.
 */
public class Entrada {

    // Usa a mesma codificação do terminal, para "João" e "Ficção" chegarem certinho
    private static final Scanner scanner = new Scanner(System.in, codificacaoDoTerminal());

    private static Charset codificacaoDoTerminal() {
        Console console = System.console();
        return (console != null) ? console.charset() : Charset.defaultCharset();
    }

    // Texto livre (pode ficar vazio)
    public static String texto(String pergunta) {
        System.out.print(pergunta + ": ");
        return scanner.nextLine().trim();
    }

    // Texto que não pode ficar vazio
    public static String textoObrigatorio(String pergunta) {
        while (true) {
            String resposta = texto(pergunta);
            if (!resposta.isEmpty()) {
                return resposta;
            }
            Tela.aviso("Este campo é obrigatório");
        }
    }

    // Número inteiro entre min e max
    public static int numero(String pergunta, int min, int max) {
        while (true) {
            String resposta = texto(pergunta + " (" + min + " a " + max + ")");
            try {
                int valor = Integer.parseInt(resposta);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // não era número: cai no aviso abaixo
            }
            Tela.aviso("Digite um número de " + min + " a " + max);
        }
    }

    // Número sem limite definido (ex: id de livro). 0 = cancelar.
    public static int id(String pergunta) {
        return naoNegativo(pergunta + " (0 para voltar)");
    }

    // Número 0 ou maior, sem texto extra na pergunta
    public static int naoNegativo(String pergunta) {
        while (true) {
            String resposta = texto(pergunta);
            try {
                int valor = Integer.parseInt(resposta);
                if (valor >= 0) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // não era número: cai no aviso abaixo
            }
            Tela.aviso("Digite o número (id) que aparece na lista");
        }
    }

    // Vários números separados por vírgula: "1, 4, 5"
    public static List<Integer> listaDeNumeros(String pergunta) {
        while (true) {
            String resposta = texto(pergunta);
            List<Integer> numeros = new ArrayList<>();
            try {
                for (String parte : resposta.split(",")) {
                    if (!parte.isBlank()) {
                        numeros.add(Integer.parseInt(parte.trim()));
                    }
                }
                return numeros;
            } catch (NumberFormatException e) {
                Tela.aviso("Use só números separados por vírgula, ex: 1, 4, 5");
            }
        }
    }

    public static boolean simNao(String pergunta) {
        String resposta = texto(pergunta + " (s/n)").toLowerCase();
        return resposta.startsWith("s");
    }

    public static void pausar() {
        texto("\n[Enter para continuar]");
    }
}
