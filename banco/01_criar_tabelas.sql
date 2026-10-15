-- =========================================
-- BookSwipe Sustentável - criação do banco
-- =========================================

-- Apaga e recria do zero (pode rodar o script quantas vezes quiser)
DROP DATABASE IF EXISTS bookswipe;

CREATE DATABASE bookswipe;

USE bookswipe;

-- ---------- PESSOAS ----------

-- Quem usa o sistema (alunos)
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    curso VARCHAR(100),
    bairro VARCHAR(100), -- usado no marketplace de trocas
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Quem está logado no site: um token por login.
-- Guarda o HASH (SHA-256) do token, não o token: se o banco vazar, os tokens não servem.
CREATE TABLE sessao (
    token_hash CHAR(64) PRIMARY KEY,
    usuario_id INT NOT NULL,
    criada_em DATETIME DEFAULT CURRENT_TIMESTAMP,
    expira_em DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- ---------- CATÁLOGO ----------

-- Categorias de livro (Fantasia, Romance...)
CREATE TABLE genero (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- A obra (cada título aparece uma vez só)
CREATE TABLE livro (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    isbn VARCHAR(20),
    sinopse TEXT, -- vem da Google Books API
    capa_url VARCHAR(500), -- vem da Google Books API
    genero_id INT,
    FOREIGN KEY (genero_id) REFERENCES genero (id)
);

-- Gêneros favoritos de cada usuário (N:N) - base do match
CREATE TABLE usuario_genero (
    usuario_id INT NOT NULL,
    genero_id INT NOT NULL,
    PRIMARY KEY (usuario_id, genero_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (genero_id) REFERENCES genero (id)
);

-- "Minha estante": o que o usuário está lendo, já leu ou quer ler
CREATE TABLE estante (
    usuario_id INT NOT NULL,
    livro_id INT NOT NULL,
    status VARCHAR(20) NOT NULL, -- QUERO_LER, LENDO, LIDO
    favorito BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (usuario_id, livro_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (livro_id) REFERENCES livro (id)
);

-- ---------- REDE SOCIAL ----------

-- O "post": a experiência de um usuário lendo um livro
CREATE TABLE experiencia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    livro_id INT NOT NULL,
    nota INT NOT NULL CHECK (nota BETWEEN 0 AND 5),
    sentimento VARCHAR(50), -- ex: Emocionado, Empolgado...
    texto TEXT,
    trecho_favorito VARCHAR(500),
    data_post DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (livro_id) REFERENCES livro (id)
);

-- O swipe: ❤️ GOSTEI ou ❌ PASSAR em uma experiência
CREATE TABLE curtida (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    experiencia_id INT NOT NULL,
    tipo VARCHAR(10) NOT NULL, -- GOSTEI, PASSAR
    data_curtida DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (usuario_id, experiencia_id), -- cada um vota 1 vez por post
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (experiencia_id) REFERENCES experiencia (id)
);

-- Comentários em uma experiência
CREATE TABLE comentario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    experiencia_id INT NOT NULL,
    texto VARCHAR(1000) NOT NULL,
    data_comentario DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (experiencia_id) REFERENCES experiencia (id)
);

-- ---------- TROCA SUSTENTÁVEL ----------

-- Um exemplar físico que o usuário oferece para troca
CREATE TABLE livro_disponivel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    livro_id INT NOT NULL,
    condicao VARCHAR(20) NOT NULL, -- NOVO, BOA, REGULAR
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL', -- DISPONIVEL, RESERVADO, TROCADO
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (livro_id) REFERENCES livro (id)
);

-- Alguém pedindo um livro disponível (CONCLUIDA = troca feita)
CREATE TABLE solicitacao_troca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    livro_disponivel_id INT NOT NULL,
    solicitante_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE', -- PENDENTE, ACEITA, RECUSADA, CONCLUIDA
    data_solicitacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_conclusao DATETIME,
    FOREIGN KEY (livro_disponivel_id) REFERENCES livro_disponivel (id),
    FOREIGN KEY (solicitante_id) REFERENCES usuario (id)
);