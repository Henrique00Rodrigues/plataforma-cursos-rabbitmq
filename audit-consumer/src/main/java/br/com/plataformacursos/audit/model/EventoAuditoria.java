package br.com.plataformacursos.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eventos_auditoria")
public class EventoAuditoria {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "tipo_evento")
    private String tipoEvento;

    @Column(name = "ocorrido_em")
    private Instant ocorridoEm;

    @Column(name = "payload")
    private String payload;

    protected EventoAuditoria() {
    }
}