package com.bookswipe.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexões com o MySQL usando os dados do arquivo db.properties.
 * Todas as classes DAO pedem a conexão por aqui.
 */
public class ConexaoBanco {

    private static final Properties config = carregarConfig();

    private static Properties carregarConfig() {
        Properties props = new Properties();
        try (InputStream arquivo = ConexaoBanco.class.getResourceAsStream("/db.properties")) {
            if (arquivo == null) {
                throw new RuntimeException("Arquivo db.properties não encontrado em src/main/resources");
            }
            props.load(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler db.properties", e);
        }
        return props;
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.usuario"),
                config.getProperty("db.senha"));
    }
}
