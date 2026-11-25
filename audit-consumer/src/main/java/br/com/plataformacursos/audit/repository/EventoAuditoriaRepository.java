package br.com.plataformacursos.audit.repository;

import br.com.plataformacursos.audit.model.EventoAuditoria;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EventoAuditoriaRepository extends Repository<EventoAuditoria, UUID> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO eventos_auditoria (evento_id, tipo_evento, ocorrido_em, payload)
        VALUES (:eventoId, :tipoEvento, :ocorridoEm, :payload)
        ON CONFLICT (evento_id) DO NOTHING
        """, nativeQuery = true)
    int inserirSeAusente(
        @Param("eventoId") UUID eventoId,
        @Param("tipoEvento") String tipoEvento,
        @Param("ocorridoEm") Instant ocorridoEm,
        @Param("payload") String payload
    );
}