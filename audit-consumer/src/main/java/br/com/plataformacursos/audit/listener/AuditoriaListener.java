package br.com.plataformacursos.audit.listener;

import br.com.plataformacursos.audit.repository.EventoAuditoriaRepository;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuditoriaListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaListener.class);

    private final JsonMapper jsonMapper;
    private final EventoAuditoriaRepository repositorio;

    public AuditoriaListener(JsonMapper jsonMapper, EventoAuditoriaRepository repositorio) {
        this.jsonMapper = jsonMapper;
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = "fila.auditoria")
    public void receber(Message mensagem, Channel canal) throws IOException {
        long entrega = mensagem.getMessageProperties().getDeliveryTag();
        String payload = new String(mensagem.getBody(), StandardCharsets.UTF_8);

        EventoAcademico evento;
        try {
            evento = jsonMapper.readValue(payload, EventoAcademico.class);

            if (evento == null || evento.eventoId() == null
                || evento.tipo() == null || evento.tipo().isBlank()
                || evento.ocorridoEm() == null || evento.dados() == null) {
                throw new IllegalArgumentException("Evento incompleto");
            }
        } catch (JacksonException | IllegalArgumentException erro) {
            LOG.warn("Mensagem de auditoria inválida: {}", erro.getMessage());
            canal.basicReject(entrega, false);
            return;
        }

        int inseridos;
        try {
            inseridos = repositorio.inserirSeAusente(
                evento.eventoId(),
                evento.tipo(),
                evento.ocorridoEm().toInstant(),
                payload
            );
        } catch (DataAccessException | TransactionException erro) {
            LOG.error("Falha ao auditar o evento {}", evento.eventoId(), erro);
            canal.basicNack(entrega, false, true);
            return;
        }

        canal.basicAck(entrega, false);

        if (inseridos == 1) {
            LOG.info("Evento auditado: {} ({})", evento.tipo(), evento.eventoId());
        } else {
            LOG.info("Evento já auditado: {}", evento.eventoId());
        }
    }
}