CREATE TABLE IF NOT EXISTS avisos (
    evento_id UUID PRIMARY KEY,
    tipo_evento VARCHAR(50) NOT NULL,
    aluno_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    avaliacao_id BIGINT,
    texto TEXT NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);