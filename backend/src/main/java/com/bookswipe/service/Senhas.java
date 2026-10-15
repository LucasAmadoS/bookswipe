package com.bookswipe.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hash de senha com BCrypt.
 *
 * O banco NUNCA guarda a senha, só o hash:
 *   "123456" -> "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
 *
 * - Não dá para voltar do hash para a senha (é uma via só).
 * - Cada hash leva um "sal" aleatório: duas pessoas com a mesma senha ficam com hashes diferentes.
 * - O BCrypt é lento de propósito (custo 10 = 2^10 rodadas): testar milhões de senhas fica caro para um invasor.
 */
public class Senhas {

    private static final int CUSTO = 10;

    public static String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO));
    }

    // O BCrypt tira o sal de dentro do hash salvo, refaz a conta com a senha digitada e compara
    public static boolean confere(String senhaDigitada, String hashSalvo) {
        if (hashSalvo == null || !ehHash(hashSalvo)) {
            return false;
        }
        return BCrypt.checkpw(senhaDigitada, hashSalvo);
    }

    // Todo hash BCrypt começa com "$2a$", "$2b$" ou "$2y$"
    public static boolean ehHash(String texto) {
        return texto != null && texto.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
