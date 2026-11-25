package br.com.plataformacursos.indicators.listener;

import br.com.plataformacursos.indicators.repository.IndicadorRepository;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import br.com.plataformacursos.shared.evento.TipoEvento;
import br.com.plataformacursos.shared.evento.dados.CursoConcluidoDados;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class IndicadoresListener {

    private static final Logger LOG = LoggerFactory.getLogger(IndicadoresListener.class);

    private final JsonMapper jsonMapper;
    private final IndicadorRepository repositorio;
    private final String instancia;

    public IndicadoresListener(
        JsonMapper jsonMapper,
        IndicadorRepository repositorio,
        @Value("${indicadores.instancia:local}") String instancia
    ) {
        this.jsonMapper = jsonMapper;
        this.repositorio = repositorio;
        this.instancia = instancia;
    }

    @RabbitListener(queues = "fila.indicadores")
    public void receber(Message mensagem, Channel canal) throws IOException {
        long entrega = mensagem.getMessageProperties().getDeliveryTag();

        EventoAcademico evento;
        CursoConcluidoDados dados;
        try {
            evento = jsonMapper.readValue(mensagem.getBody(), EventoAcademico.class);

            if (evento == null || evento.eventoId() == null || evento.dados() == null
                || !TipoEvento.CURSO_CONCLUIDO.getTipo().equals(evento.tipo())) {
                throw new IllegalArgumentException("Esperado um evento CursoConcluido");
            }

            dados = jsonMapper.convertValue(evento.dados(), CursoConcluidoDados.class);
            if (dados.cursoId() == null || dados.cursoId() <= 0) {
                throw new IllegalArgumentException("O identificador do curso deve ser positivo");
            }
        } catch (JacksonException | IllegalArgumentException erro) {
            LOG.warn("Mensagem de indicadores inválida: {}", erro.getMessage());
            canal.basicReject(entrega, false);
            return;
        }

        boolean contabilizado;
        try {
            contabilizado = repositorio.registrarConclusao(
                evento.eventoId(), dados.cursoId(), instancia
            );
        } catch (DataAccessException | TransactionException erro) {
            LOG.error("Falha ao contabilizar o evento {}", evento.eventoId(), erro);
            canal.basicNack(entrega, false, true);
            return;
        }

        canal.basicAck(entrega, false);

        if (contabilizado) {
            LOG.info("[{}] Contabilizou o evento {} no curso {}",
                instancia, evento.eventoId(), dados.cursoId());
        } else {
            LOG.info("[{}] Evento já contabilizado: {}", instancia, evento.eventoId());
        }
    }
}