package br.com.plataformacursos.producer.service;

import br.com.plataformacursos.producer.config.RabbitMqConfig;
import br.com.plataformacursos.producer.repository.EventoRepository;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import br.com.plataformacursos.shared.evento.TipoEvento;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
public class EventoPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;
    private final EventoRepository repositorio;

    public EventoPublisher(
        RabbitTemplate rabbitTemplate,
        JsonMapper jsonMapper,
        EventoRepository repositorio
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
        this.repositorio = repositorio;
    }

    public EventoAcademico publicar(TipoEvento tipo, Record dadosDoEvento) {
        Map<String, Object> dados = jsonMapper.convertValue(
            dadosDoEvento, new TypeReference<Map<String, Object>>() {}
        );

        EventoAcademico evento = new EventoAcademico(
            UUID.randomUUID(),
            tipo.getTipo(),
            OffsetDateTime.now(),
            dados
        );

        String routingKey = tipo.getRoutingKey();
        String payload = jsonMapper.writeValueAsString(evento);

        repositorio.salvar(evento, routingKey, payload);
        enviar(evento.eventoId(), routingKey, payload);

        return evento;
    }

    public boolean republicar(UUID eventoId) {
    var resultado = repositorio.buscarPorId(eventoId);

    if (resultado.isEmpty()) {
        return false;
    }

    EventoRepository.EventoSalvo evento = resultado.get();
    enviar(eventoId, evento.routingKey(), evento.payload());
    return true;
    }

    private void enviar(UUID eventoId, String routingKey, String payload) {
        Message mensagem = MessageBuilder
            .withBody(payload.getBytes(StandardCharsets.UTF_8))
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
            .setMessageId(eventoId.toString())
            .build();

        rabbitTemplate.send(
            RabbitMqConfig.EXCHANGE_EVENTOS,
            routingKey,
            mensagem
        );
    }
}