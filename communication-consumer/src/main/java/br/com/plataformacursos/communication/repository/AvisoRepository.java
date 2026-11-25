package br.com.plataformacursos.communication.repository;

import br.com.plataformacursos.communication.model.Aviso;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AvisoRepository extends Repository<Aviso, UUID> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO avisos (
            evento_id, tipo_evento, aluno_id, curso_id, avaliacao_id, texto
        )
        VALUES (:eventoId, :tipoEvento, :alunoId, :cursoId, :avaliacaoId, :texto)
        ON CONFLICT (evento_id) DO NOTHING
        """, nativeQuery = true)
    int inserirSeAusente(
        @Param("eventoId") UUID eventoId,
        @Param("tipoEvento") String tipoEvento,
        @Param("alunoId") Long alunoId,
        @Param("cursoId") Long cursoId,
        @Param("avaliacaoId") Long avaliacaoId,
        @Param("texto") String texto
    );
}