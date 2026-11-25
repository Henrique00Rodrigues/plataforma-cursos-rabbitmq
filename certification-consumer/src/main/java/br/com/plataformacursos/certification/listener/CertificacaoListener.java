package br.com.plataformacursos.certification.listener;

import br.com.plataformacursos.certification.repository.CertificadoRepository;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import br.com.plataformacursos.shared.evento.TipoEvento;
import br.com.plataformacursos.shared.evento.dados.CursoConcluidoDados;
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
public class CertificacaoListener {

    private static final Logger LOG = LoggerFactory.getLogger(CertificacaoListener.class);

    private final JsonMapper jsonMapper;
    private final CertificadoRepository repositorio;

    public CertificacaoListener(JsonMapper jsonMapper, CertificadoRepository repositorio) {
        this.jsonMapper = jsonMapper;
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = "fila.certificacao")
    public void receber(Message mensagem, Channel canal) throws IOException {
        long entrega = mensagem.getMessageProperties().getDeliveryTag();

        EventoAcademico evento;
        CursoConcluidoDados dados;
        try {
            evento = jsonMapper.readValue(mensagem.getBody(), EventoAcademico.class);
            dados = lerDados(evento);
        } catch (JacksonException | IllegalArgumentException erro) {
            LOG.warn("Mensagem de certificação inválida: {}", erro.getMessage());
            canal.basicReject(entrega, false);
            return;
        }

        int inseridos;
        try {
            inseridos = repositorio.inserirSeAusente(
                evento.eventoId(), dados.alunoId(), dados.cursoId(), dados.cargaHoraria()
            );
        } catch (DataAccessException | TransactionException erro) {
            LOG.error("Falha ao gravar o certificado do evento {}", evento.eventoId(), erro);
            canal.basicNack(entrega, false, true);
            return;
        }

        canal.basicAck(entrega, false);

        if (inseridos == 1) {
            LOG.info("Certificado emitido para o evento {}", evento.eventoId());
        } else {
            LOG.info("Certificado já registrado para o evento {}", evento.eventoId());
        }
    }

    private CursoConcluidoDados lerDados(EventoAcademico evento) {
        if (evento == null || evento.eventoId() == null || evento.dados() == null
            || !TipoEvento.CURSO_CONCLUIDO.getTipo().equals(evento.tipo())) {
            throw new IllegalArgumentException("Esperado um CursoConcluido com identificador e dados");
        }

        CursoConcluidoDados dados = jsonMapper.convertValue(
            evento.dados(), CursoConcluidoDados.class
        );

        if (dados.alunoId() == null || dados.alunoId() <= 0
            || dados.cursoId() == null || dados.cursoId() <= 0
            || dados.cargaHoraria() == null || dados.cargaHoraria() <= 0) {
            throw new IllegalArgumentException("Aluno, curso e carga horária devem ser positivos");
        }

        return dados;
    }
}