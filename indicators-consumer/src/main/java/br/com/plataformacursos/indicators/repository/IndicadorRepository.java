package br.com.plataformacursos.indicators.repository;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class IndicadorRepository {

    private final JdbcClient jdbc;

    public IndicadorRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean registrarConclusao(UUID eventoId, Long cursoId, String instancia) {
        int linhasAfetadas = jdbc.sql("""
            WITH evento_novo AS (
                INSERT INTO eventos_indicadores (evento_id, curso_id, processado_por)
                VALUES (:eventoId, :cursoId, :instancia)
                ON CONFLICT (evento_id) DO NOTHING
                RETURNING curso_id
            )
            INSERT INTO indicadores_curso (curso_id, total_conclusoes)
            SELECT curso_id, 1
            FROM evento_novo
            ON CONFLICT (curso_id) DO UPDATE
            SET total_conclusoes = indicadores_curso.total_conclusoes + 1
            """)
            .param("eventoId", eventoId)
            .param("cursoId", cursoId)
            .param("instancia", instancia)
            .update();

        return linhasAfetadas == 1;
    }
}