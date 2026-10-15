package com.bookswipe;

import com.bookswipe.dao.ConexaoBanco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Teste rápido: conecta no banco e lista os usuários.
 * Se aparecer a lista, o Java está falando com o MySQL.
 */
public class TesteConexao {

    public static void main(String[] args) {
        String sql = "SELECT id, nome, bairro FROM usuario ORDER BY id";

        try (Connection conexao = ConexaoBanco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            System.out.println("Conectado ao MySQL! Usuários cadastrados:");
            while (resultado.next()) {
                System.out.printf("  %d - %s (%s)%n",
                        resultado.getInt("id"),
                        resultado.getString("nome"),
                        resultado.getString("bairro"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
        }
    }
}
