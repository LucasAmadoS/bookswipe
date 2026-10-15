-- =========================================
-- BookSwipe Sustentável - dados de teste
-- Rode DEPOIS do 01_criar_tabelas.sql
-- =========================================

USE bookswipe;

-- ---------- GÊNEROS ----------
INSERT INTO
    genero (id, nome)
VALUES (1, 'Fantasia'),
    (2, 'Romance'),
    (3, 'Terror'),
    (4, 'Ficção'),
    (5, 'Suspense'),
    (6, 'História');

-- ---------- USUÁRIOS ----------
-- Senhas guardadas com hash BCrypt. A senha de todos os usuários de teste é: 123456
INSERT INTO
    usuario (
        id,
        nome,
        email,
        senha,
        curso,
        bairro,
        data_cadastro
    )
VALUES (
        1,
        'Lucas Amado',
        'lucas@bookswipe.com',
        '$2a$10$YJbcqLqQdzRmk7oJtiaYWOa5UmyeWLbPROskiYYXdjD6sUWXoZvoG',
        'Análise e Desenvolvimento de Sistemas',
        'Centro',
        '2026-05-02 10:00:00'
    ),
    (
        2,
        'Ana Souza',
        'ana@bookswipe.com',
        '$2a$10$m3rzQGVVztZgzZmktztpOO7mV3zinj6cnluK6nOeYBDasDMjsbDp6',
        'Direito',
        'Campolim',
        '2026-05-03 14:20:00'
    ),
    (
        3,
        'João Silva',
        'joao@bookswipe.com',
        '$2a$10$eTow7kJ5ypW1CRGm95R4GulVD8ihLJYv5VswlsYcf6I.vjujCU.qC',
        'Engenharia Civil',
        'Jardim Vergueiro',
        '2026-05-05 09:15:00'
    ),
    (
        4,
        'Eliane Rocha',
        'eliane@bookswipe.com',
        '$2a$10$y87GuRWVhAxk7Tev0vcUFuZfNC0/jm29ct3vMqYBgeYjE0phlM3je',
        'Bibliotecária',
        'Centro',
        '2026-05-06 08:00:00'
    ),
    (
        5,
        'Mariana Costa',
        'mariana@bookswipe.com',
        '$2a$10$514EE72GZEb2b9WxiAJW6.0GofqWF2yyPFxk1JFFsxTdafcZ3jIBW',
        'Psicologia',
        'Éden',
        '2026-05-10 19:40:00'
    ),
    (
        6,
        'Pedro Lima',
        'pedro@bookswipe.com',
        '$2a$10$O/Oh9dAqdhHg.FqY8yu0JeJo4PC/rfsU5kCbnayY6gkph.WoNRKzW',
        'Análise e Desenvolvimento de Sistemas',
        'Wanel Ville',
        '2026-05-12 21:05:00'
    );

-- Gêneros favoritos (tela "Crie seu perfil de leitor")
INSERT INTO
    usuario_genero (usuario_id, genero_id)
VALUES (1, 1),
    (1, 4),
    (1, 5), -- Lucas: Fantasia, Ficção, Suspense
    (2, 1),
    (2, 2),
    (2, 4), -- Ana: Fantasia, Romance, Ficção
    (3, 4),
    (3, 6), -- João: Ficção, História
    (4, 2),
    (4, 6), -- Eliane: Romance, História
    (5, 3),
    (5, 5), -- Mariana: Terror, Suspense
    (6, 1),
    (6, 3);
-- Pedro: Fantasia, Terror

-- ---------- LIVROS ----------
INSERT INTO
    livro (
        id,
        titulo,
        autor,
        sinopse,
        genero_id
    )
VALUES (
        1,
        'O Hobbit',
        'J.R.R. Tolkien',
        'Bilbo Bolseiro é um hobbit que vive uma vida pacata até ser surpreendido por Gandalf e levado a uma aventura inesquecível.',
        1
    ),
    (
        2,
        '1984',
        'George Orwell',
        'Em um regime totalitário que vigia cada passo da população, Winston Smith começa a questionar o Grande Irmão.',
        4
    ),
    (
        3,
        'Harry Potter e a Pedra Filosofal',
        'J.K. Rowling',
        'Harry descobre no seu aniversário de 11 anos que é um bruxo e vai estudar em Hogwarts.',
        1
    ),
    (
        4,
        'Jogos Vorazes',
        'Suzanne Collins',
        'Katniss se oferece no lugar da irmã para participar de um jogo mortal transmitido para todo o país.',
        4
    ),
    (
        5,
        'Dom Casmurro',
        'Machado de Assis',
        'Bentinho relembra sua vida e seu amor por Capitu, sempre com a dúvida: ela o traiu ou não?',
        2
    ),
    (
        6,
        'O Iluminado',
        'Stephen King',
        'Jack aceita cuidar de um hotel isolado durante o inverno, com a esposa e o filho, e coisas estranhas começam a acontecer.',
        3
    ),
    (
        7,
        'Orgulho e Preconceito',
        'Jane Austen',
        'Elizabeth Bennet e o orgulhoso Sr. Darcy vivem uma história de desencontros e primeiras impressões erradas.',
        2
    ),
    (
        8,
        'Sapiens',
        'Yuval Noah Harari',
        'Uma breve história da humanidade, da Idade da Pedra até os dias de hoje.',
        6
    ),
    (
        9,
        'O Código Da Vinci',
        'Dan Brown',
        'Um assassinato no Louvre leva o professor Robert Langdon a uma corrida para desvendar um segredo antigo.',
        5
    ),
    (
        10,
        'Duna',
        'Frank Herbert',
        'Paul Atreides se muda com a família para Arrakis, o planeta deserto mais valioso do universo.',
        4
    );

-- ---------- MINHA ESTANTE ----------
INSERT INTO
    estante (
        usuario_id,
        livro_id,
        status,
        favorito
    )
VALUES (1, 1, 'LIDO', TRUE),
    (1, 2, 'LIDO', FALSE),
    (1, 3, 'LIDO', TRUE),
    (1, 10, 'QUERO_LER', FALSE),
    (1, 9, 'LENDO', FALSE),
    (2, 1, 'LIDO', TRUE),
    (2, 3, 'LIDO', FALSE),
    (2, 7, 'LIDO', TRUE),
    (2, 5, 'LENDO', FALSE),
    (3, 2, 'LIDO', TRUE),
    (3, 8, 'LIDO', FALSE),
    (3, 10, 'LIDO', TRUE),
    (3, 1, 'LENDO', FALSE),
    (4, 5, 'LIDO', TRUE),
    (4, 8, 'LENDO', FALSE),
    (5, 6, 'LIDO', TRUE),
    (5, 9, 'LIDO', FALSE),
    (5, 3, 'QUERO_LER', FALSE),
    (6, 3, 'LIDO', TRUE),
    (6, 6, 'LIDO', FALSE),
    (6, 1, 'QUERO_LER', FALSE);

-- ---------- EXPERIÊNCIAS (posts) ----------
INSERT INTO
    experiencia (
        id,
        usuario_id,
        livro_id,
        nota,
        sentimento,
        texto,
        trecho_favorito,
        data_post
    )
VALUES (
        1,
        1,
        1,
        5,
        'Empolgado',
        'Gostei muito da aventura. O Bilbo crescendo ao longo da história é o melhor do livro.',
        'Nem todos os que vagueiam estão perdidos.',
        '2026-06-01 20:10:00'
    ),
    (
        2,
        1,
        2,
        4,
        'Pensativo',
        'Assustador como algumas coisas parecem atuais. Leitura pesada, mas necessária.',
        'Guerra é paz. Liberdade é escravidão.',
        '2026-06-15 22:30:00'
    ),
    (
        3,
        1,
        3,
        5,
        'Nostálgico',
        'Reli depois de anos e continua mágico.',
        NULL,
        '2026-07-02 18:00:00'
    ),
    (
        4,
        2,
        1,
        5,
        'Encantada',
        'Uma das melhores aventuras que já li, dá vontade de entrar no livro.',
        NULL,
        '2026-06-03 13:45:00'
    ),
    (
        5,
        2,
        3,
        4,
        'Feliz',
        'Leitura leve e divertida, perfeita para começar a ler fantasia.',
        NULL,
        '2026-06-20 16:00:00'
    ),
    (
        6,
        2,
        7,
        5,
        'Apaixonada',
        'Elizabeth é uma personagem incrível. Os diálogos são ótimos.',
        NULL,
        '2026-07-10 21:15:00'
    ),
    (
        7,
        3,
        2,
        5,
        'Impactado',
        'Mudou o jeito que eu vejo tecnologia e vigilância.',
        NULL,
        '2026-06-08 11:30:00'
    ),
    (
        8,
        3,
        8,
        4,
        'Curioso',
        'Muito interessante, mas em alguns capítulos fica repetitivo.',
        NULL,
        '2026-07-18 09:50:00'
    ),
    (
        9,
        5,
        6,
        5,
        'Assustada',
        'Li com a luz acesa. O suspense psicológico é absurdo.',
        NULL,
        '2026-06-25 23:59:00'
    ),
    (
        10,
        5,
        9,
        3,
        'Entretida',
        'Prende do começo ao fim, mas o final foi previsível.',
        NULL,
        '2026-08-02 15:20:00'
    ),
    (
        11,
        6,
        3,
        4,
        'Nostálgico',
        'Livro que me fez gostar de ler quando era criança.',
        NULL,
        '2026-07-05 19:00:00'
    ),
    (
        12,
        6,
        6,
        4,
        'Tenso',
        'Muito melhor que o filme.',
        NULL,
        '2026-08-11 22:40:00'
    ),
    (
        13,
        4,
        5,
        5,
        'Reflexiva',
        'Um clássico que todo aluno deveria ler. Capitu traiu ou não? Cada leitor decide.',
        'Olhos de ressaca.',
        '2026-06-12 10:00:00'
    ),
    (
        14,
        3,
        10,
        4,
        'Empolgado',
        'Mundo muito bem construído. O começo é lento, depois não dá para parar.',
        NULL,
        '2026-09-01 20:20:00'
    );

-- ---------- CURTIDAS (swipe) ----------
INSERT INTO
    curtida (
        usuario_id,
        experiencia_id,
        tipo
    )
VALUES (2, 1, 'GOSTEI'),
    (3, 1, 'GOSTEI'),
    (6, 1, 'GOSTEI'),
    (5, 1, 'PASSAR'),
    (1, 4, 'GOSTEI'),
    (6, 4, 'GOSTEI'),
    (2, 3, 'GOSTEI'),
    (6, 3, 'GOSTEI'),
    (1, 5, 'GOSTEI'),
    (6, 5, 'GOSTEI'),
    (3, 2, 'GOSTEI'),
    (1, 7, 'GOSTEI'),
    (2, 7, 'PASSAR'),
    (4, 6, 'GOSTEI'),
    (1, 6, 'PASSAR'),
    (6, 9, 'GOSTEI'),
    (5, 12, 'GOSTEI'),
    (1, 10, 'GOSTEI'),
    (2, 13, 'GOSTEI'),
    (3, 13, 'GOSTEI'),
    (1, 14, 'GOSTEI'),
    (4, 8, 'GOSTEI'),
    (1, 11, 'GOSTEI');

-- ---------- COMENTÁRIOS ----------
INSERT INTO
    comentario (
        usuario_id,
        experiencia_id,
        texto,
        data_comentario
    )
VALUES (
        2,
        1,
        'Concordo! A cena das charadas com o Gollum é a minha favorita.',
        '2026-06-01 21:00:00'
    ),
    (
        3,
        1,
        'Esse livro me fez querer ler O Senhor dos Anéis.',
        '2026-06-02 08:30:00'
    ),
    (
        1,
        4,
        'Bom saber que você também curtiu!',
        '2026-06-03 15:00:00'
    ),
    (
        1,
        7,
        'Faz pensar muito nas redes sociais hoje.',
        '2026-06-08 12:10:00'
    ),
    (
        6,
        9,
        'Esse é o melhor do Stephen King pra mim.',
        '2026-06-26 10:00:00'
    ),
    (
        2,
        13,
        'Eu acho que traiu! Rs',
        '2026-06-12 14:30:00'
    );

-- ---------- TROCAS ----------
-- Exemplares físicos oferecidos
INSERT INTO
    livro_disponivel (
        id,
        usuario_id,
        livro_id,
        condicao,
        status
    )
VALUES (1, 1, 1, 'BOA', 'TROCADO'), -- O Hobbit do Lucas
    (
        2,
        1,
        2,
        'REGULAR',
        'DISPONIVEL'
    ), -- 1984 do Lucas
    (3, 2, 7, 'NOVO', 'DISPONIVEL'), -- Orgulho e Preconceito da Ana
    (4, 3, 8, 'BOA', 'TROCADO'), -- Sapiens do João
    (5, 5, 6, 'BOA', 'RESERVADO'), -- O Iluminado da Mariana
    (6, 4, 5, 'BOA', 'TROCADO'), -- Dom Casmurro da Eliane (doação da biblioteca)
    (
        7,
        6,
        3,
        'REGULAR',
        'DISPONIVEL'
    ), -- Harry Potter do Pedro
    (8, 2, 3, 'BOA', 'TROCADO'), -- Harry Potter da Ana
    (9, 3, 10, 'BOA', 'DISPONIVEL');
-- Duna do João

-- Pedidos de troca (CONCLUIDA = livro voltou a circular ♻️)
INSERT INTO
    solicitacao_troca (
        livro_disponivel_id,
        solicitante_id,
        status,
        data_solicitacao,
        data_conclusao
    )
VALUES (
        1,
        3,
        'CONCLUIDA',
        '2026-06-02 10:00:00',
        '2026-06-05 17:00:00'
    ), -- João pegou O Hobbit do Lucas
    (
        4,
        4,
        'CONCLUIDA',
        '2026-07-10 09:00:00',
        '2026-07-14 12:00:00'
    ), -- Eliane pegou Sapiens do João
    (
        6,
        2,
        'CONCLUIDA',
        '2026-08-03 15:30:00',
        '2026-08-06 18:00:00'
    ), -- Ana pegou Dom Casmurro
    (
        8,
        5,
        'CONCLUIDA',
        '2026-09-12 11:00:00',
        '2026-09-15 16:00:00'
    ), -- Mariana pegou Harry Potter da Ana
    (
        5,
        6,
        'ACEITA',
        '2026-10-01 20:00:00',
        NULL
    ), -- Pedro vai pegar O Iluminado
    (
        7,
        5,
        'RECUSADA',
        '2026-09-10 14:00:00',
        NULL
    ), -- Mariana pediu HP do Pedro (já conseguiu outro)
    (
        9,
        2,
        'RECUSADA',
        '2026-09-20 18:00:00',
        NULL
    ),
    (
        9,
        1,
        'PENDENTE',
        '2026-10-12 22:10:00',
        NULL
    );
-- Lucas quer Duna do João

-- ---------- CONFERÊNCIA ----------
SELECT 'usuarios' AS tabela, COUNT(*) AS total
FROM usuario
UNION ALL
SELECT 'livros', COUNT(*)
FROM livro
UNION ALL
SELECT 'experiencias', COUNT(*)
FROM experiencia
UNION ALL
SELECT 'curtidas', COUNT(*)
FROM curtida
UNION ALL
SELECT 'trocas feitas', COUNT(*)
FROM solicitacao_troca
WHERE
    status = 'CONCLUIDA';