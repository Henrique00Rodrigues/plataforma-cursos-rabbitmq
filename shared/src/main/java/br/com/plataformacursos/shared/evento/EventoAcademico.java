package br.com.plataformacursos.shared.evento;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record EventoAcademico(
    UUID eventoId,
    String tipo,
    OffsetDateTime ocorridoEm,
    Map<String, Object> dados
) {}
