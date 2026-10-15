package com.bookswipe;

import com.bookswipe.dao.LivroDAO;
import com.bookswipe.model.Livro;
import com.bookswipe.model.SugestaoLivro;
import com.bookswipe.service.CatalogoExternoService;
import com.bookswipe.service.RegraNegocioException;

import java.sql.SQLException;

/**
 * Testa o consumo da API pública de livros.
 * ATENÇÃO: o passo 2 GRAVA no banco a capa e o ISBN dos livros que ainda não têm capa.
 */
public class TesteCatalogoExterno {

    public static void main(String[] args) {
        CatalogoExternoService catalogo = new CatalogoExternoService();
        System.out.println("Fonte: " + catalogo.fonte());

        try {
            System.out.println("\n1) Buscando \"dom casmurro\" na internet:");
            for (SugestaoLivro s : catalogo.buscar("dom casmurro")) {
                System.out.println("   " + s.titulo() + " — " + s.autor() + (s.capaUrl() != null ? "  [tem capa]" : ""));
            }

            System.out.println("\n2) Completando capas dos livros do banco:");
            int n = catalogo.completarLivrosSemCapa();
            System.out.println("   " + n + " livro(s) completado(s)");

            System.out.println("\n3) Como ficou:");
            for (Livro l : new LivroDAO().listarTodos()) {
                System.out.println("   " + l.getTitulo() + " | ISBN " + l.getIsbn() + " | " + l.getCapaUrl());
            }
        } catch (RegraNegocioException e) {
            System.out.println("Regra: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}
