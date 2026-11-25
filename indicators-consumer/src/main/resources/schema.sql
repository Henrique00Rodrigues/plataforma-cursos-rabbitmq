CREATE TABLE IF NOT EXISTS indicadores_curso (
    curso_id BIGINT PRIMARY KEY,
    total_conclusoes BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS eventos_indicadores (
    evento_id UUID PRIMARY KEY,
    curso_id BIGINT NOT NULL,
    processado_por TEXT NOT NULL
);