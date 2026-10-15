-- =========================================
-- BookSwipe - capas e ISBN dos livros de teste
-- Buscados na Open Library pelo CatalogoExternoService.
-- Rode DEPOIS do 02_dados_teste.sql
-- =========================================

USE bookswipe;

UPDATE livro SET isbn = '9785878600125', capa_url = 'https://covers.openlibrary.org/b/id/14627509-L.jpg' WHERE id = 1;  -- O Hobbit
UPDATE livro SET isbn = '9781472133038', capa_url = 'https://covers.openlibrary.org/b/id/9267242-L.jpg' WHERE id = 2;  -- 1984
UPDATE livro SET isbn = '9788000009957', capa_url = 'https://covers.openlibrary.org/b/id/15155833-L.jpg' WHERE id = 3;  -- Harry Potter e a Pedra Filosofal
UPDATE livro SET isbn = '9782266305754', capa_url = 'https://covers.openlibrary.org/b/id/12646537-L.jpg' WHERE id = 4;  -- Jogos Vorazes
UPDATE livro SET isbn = '9788500507007', capa_url = 'https://covers.openlibrary.org/b/id/647501-L.jpg' WHERE id = 5;  -- Dom Casmurro
UPDATE livro SET isbn = '9780450040184', capa_url = 'https://covers.openlibrary.org/b/id/12376585-L.jpg' WHERE id = 6;  -- O Iluminado
UPDATE livro SET isbn = '9781546785484', capa_url = 'https://covers.openlibrary.org/b/id/14348537-L.jpg' WHERE id = 7;  -- Orgulho e Preconceito
UPDATE livro SET isbn = '9781716994982', capa_url = 'https://covers.openlibrary.org/b/id/8634250-L.jpg' WHERE id = 8;  -- Sapiens
UPDATE livro SET isbn = '9780375432309', capa_url = 'https://covers.openlibrary.org/b/id/9255229-L.jpg' WHERE id = 9;  -- O Código Da Vinci
UPDATE livro SET isbn = '9780340839935', capa_url = 'https://covers.openlibrary.org/b/id/11481354-L.jpg' WHERE id = 10;  -- Duna
