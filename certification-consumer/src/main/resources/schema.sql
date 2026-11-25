CREATE TABLE IF NOT EXISTS certificados (
    evento_id UUID PRIMARY KEY,
    aluno_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    carga_horaria INTEGER NOT NULL,
    emitido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);