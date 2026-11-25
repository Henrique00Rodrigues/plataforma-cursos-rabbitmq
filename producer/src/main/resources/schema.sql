CREATE TABLE IF NOT EXISTS eventos_produtor (
    evento_id UUID PRIMARY KEY,
    tipo_evento TEXT NOT NULL,
    routing_key TEXT NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    payload TEXT NOT NULL
);