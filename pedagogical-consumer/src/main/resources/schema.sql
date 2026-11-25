CREATE TABLE IF NOT EXISTS historico_avaliacoes (
    evento_id UUID PRIMARY KEY,
    tipo_evento TEXT NOT NULL,
    aluno_id BIGINT,
    avaliacao_id BIGINT,
    nota NUMERIC,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    payload TEXT NOT NULL
);