-- =========================================
-- BookSwipe - tabela de sessões (token de login)
-- Para quem JÁ tem o banco criado. Quem rodar o 01 do zero já ganha esta tabela.
-- =========================================

USE bookswipe;

CREATE TABLE IF NOT EXISTS sessao (
    token_hash CHAR(64) PRIMARY KEY,
    usuario_id INT NOT NULL,
    criada_em DATETIME DEFAULT CURRENT_TIMESTAMP,
    expira_em DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
