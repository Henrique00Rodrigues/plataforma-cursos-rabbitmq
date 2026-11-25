package br.com.plataformacursos.communication.listener;

import br.com.plataformacursos.communication.repository.AvisoRepository;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rabbitmq.client.Channel;
import java.io.IOException;
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
public class ComunicacaoListener {

    private static final Logger LOG = LoggerFactory.getLogger(ComunicacaoListener.class);

    private final JsonMapper jsonMapper;
    private final AvisoRepository repositorio;

    public ComunicacaoListener(JsonMapper jsonMapper, AvisoRepository repositorio) {
        this.jsonMapper = jsonMapper;
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = "fila.comunicacao")
    public void receber(Message mensagem, Channel canal) throws IOException {
        long entrega = mensagem.getMessageProperties().getDeliveryTag();

        EventoAcademico evento;
        DadosAviso dados;
        String texto;
        try {
            evento = jsonMapper.readValue(mensagem.getBody(), EventoAcademico.class);

            if (evento == null || evento.eventoId() == null
                || evento.tipo() == null || evento.dados() == null) {
                throw new IllegalArgumentException("Evento incompleto");
            }

            dados = jsonMapper.convertValue(evento.dados(), DadosAviso.class);

            if (dados.alunoId() == null || dados.alunoId() <= 0
                || dados.cursoId() == null || dados.cursoId() <= 0) {
                throw new IllegalArgumentException("Aluno e curso devem ser informados");
            }

            texto = switch (evento.tipo()) {
                case "CursoConcluido" -> "Seu curso foi concluído.";
                case "CursoAbandonado" -> "O abandono do curso foi registrado.";
                case "AvaliacaoRevisada" -> {
                    if (dados.avaliacaoId() == null || dados.avaliacaoId() <= 0) {
                        throw new IllegalArgumentException("A avaliação deve ser informada");
                    }
                    yield "Sua avaliação foi revisada.";
                }
                default -> throw new IllegalArgumentException("Tipo de evento inesperado");
            };
        } catch (JacksonException | IllegalArgumentException erro) {
            LOG.warn("Mensagem de comunicação inválida: {}", erro.getMessage());
            canal.basicReject(entrega, false);
            return;
        }

        int inseridos;
        try {
            inseridos = repositorio.inserirSeAusente(
                evento.eventoId(),
                evento.tipo(),
                dados.alunoId(),
                dados.cursoId(),
                dados.avaliacaoId(),
                texto
            );
        } catch (DataAccessException | TransactionException erro) {
            LOG.error("Falha ao gravar o aviso do evento {}", evento.eventoId(), erro);
            canal.basicNack(entrega, false, true);
            return;
        }

        canal.basicAck(entrega, false);

        if (inseridos == 1) {
            LOG.info("Aviso registrado para o evento {}", evento.eventoId());
        } else {
            LOG.info("Aviso já registrado para o evento {}", evento.eventoId());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DadosAviso(Long alunoId, Long cursoId, Long avaliacaoId) {
    }
}