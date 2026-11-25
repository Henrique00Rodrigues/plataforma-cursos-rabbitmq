package br.com.plataformacursos.producer.repository;

import br.com.plataformacursos.shared.evento.EventoAcademico;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class EventoRepository {

    private final JdbcClient jdbc;

    public EventoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void salvar(EventoAcademico evento, String routingKey, String payload) {
        jdbc.sql("""
            INSERT INTO eventos_produtor (
                evento_id, tipo_evento, routing_key, ocorrido_em, payload
            )
            VALUES (:eventoId, :tipoEvento, :routingKey, :ocorridoEm, :payload)
            """)
            .param("eventoId", evento.eventoId())
            .param("tipoEvento", evento.tipo())
            .param("routingKey", routingKey)
            .param("ocorridoEm", evento.ocorridoEm())
            .param("payload", payload)
            .update();
    }

    public Optional<EventoSalvo> buscarPorId(UUID eventoId) {
    return jdbc.sql("""
        SELECT routing_key, payload
        FROM eventos_produtor
        WHERE evento_id = :eventoId
        """)
        .param("eventoId", eventoId)
        .query((linha, numeroDaLinha) -> new EventoSalvo(
            linha.getString("routing_key"),
            linha.getString("payload")
        ))
        .optional();
}

public record EventoSalvo(String routingKey, String payload) {}
}