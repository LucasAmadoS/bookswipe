package com.bookswipe.service;

import com.bookswipe.dao.SessaoDAO;
import com.bookswipe.dao.UsuarioDAO;
import com.bookswipe.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Token de sessão: o "crachá" que o site recebe no login e mostra em toda chamada à API.
 *
 * - O token é aleatório (32 bytes do SecureRandom): impossível de adivinhar.
 * - O banco guarda só o SHA-256 do token. Quem ler o banco não consegue usar os tokens.
 * - Vale 7 dias. Sair da conta apaga a sessão na hora.
 */
public class SessaoService {

    private static final int DIAS_DE_VALIDADE = 7;
    private static final SecureRandom aleatorio = new SecureRandom();

    private final SessaoDAO sessaoDAO = new SessaoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Depois do login: cria o token e devolve ele (é a ÚNICA vez que o token aparece inteiro)
    public String iniciar(Usuario usuario) throws SQLException {
        sessaoDAO.excluirVencidas();

        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // 43 letras/números

        sessaoDAO.criar(hash(token), usuario.getId(), LocalDateTime.now().plusDays(DIAS_DE_VALIDADE));
        return token;
    }

    // Em toda chamada: de quem é este token? (null = token inválido ou vencido)
    public Usuario usuarioDoToken(String token) throws SQLException {
        if (token == null || token.isBlank()) {
            return null;
        }
        int usuarioId = sessaoDAO.buscarUsuarioId(hash(token));
        return usuarioId == 0 ? null : usuarioDAO.buscarPorId(usuarioId);
    }

    // Sair da conta
    public void encerrar(String token) throws SQLException {
        if (token != null) {
            sessaoDAO.excluir(hash(token));
        }
    }

    // SHA-256: hash rápido, serve aqui porque o token já é aleatório e longo
    // (para SENHA usamos BCrypt, que é lento de propósito — senha de gente é fácil de adivinhar)
    private static String hash(String token) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo); // 64 caracteres hexadecimais
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
