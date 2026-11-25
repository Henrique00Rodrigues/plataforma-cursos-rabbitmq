package br.com.plataformacursos.pedagogical.listener;

import br.com.plataformacursos.pedagogical.repository.RegistroAvaliacaoRepository;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import br.com.plataformacursos.shared.evento.TipoEvento;
import br.com.plataformacursos.shared.evento.dados.AvaliacaoEnviadaDados;
import br.com.plataformacursos.shared.evento.dados.AvaliacaoRevisadaDados;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PedagogicoListener {

    private static final Logger LOG = LoggerFactory.getLogger(PedagogicoListener.class);

    private final JsonMapper jsonMapper;
    private final ObjectReader leitorEvento;
    private final RegistroAvaliacaoRepository repositorio;

    public PedagogicoListener(
        JsonMapper jsonMapper, RegistroAvaliacaoRepository repositorio
    ) {
        this.jsonMapper = jsonMapper;
        this.leitorEvento = jsonMapper.readerFor(EventoAcademico.class)
            .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = "fila.pedagogico")
    public void receber(Message mensagem, Channel canal) throws IOException {
        long entrega = mensagem.getMessageProperties().getDeliveryTag();
        String payload = new String(mensagem.getBody(), StandardCharsets.UTF_8);

        EventoAcademico evento;
        DadosHistorico dados;
        try {
            evento = leitorEvento.readValue(payload);

            if (evento == null || evento.eventoId() == null
                || evento.tipo() == null || evento.tipo().isBlank()
                || evento.ocorridoEm() == null || evento.dados() == null) {
                throw new IllegalArgumentException("Evento incompleto");
            }

            dados = extrairDados(evento);
        } catch (JacksonException | IllegalArgumentException erro) {
            LOG.warn("Mensagem pedagógica inválida: {}", erro.getMessage());
            canal.basicReject(entrega, false);
            return;
        }

        int inseridos;
        try {
            inseridos = repositorio.inserirSeAusente(
                evento.eventoId(),
                evento.tipo(),
                dados.alunoId(),
                dados.avaliacaoId(),
                dados.nota(),
                evento.ocorridoEm().toInstant(),
                payload
            );
        } catch (DataAccessException | TransactionException erro) {
            LOG.error("Falha ao registrar o evento {}", evento.eventoId(), erro);
            canal.basicNack(entrega, false, true);
            return;
        }

        canal.basicAck(entrega, false);

        if (inseridos == 1) {
            LOG.info("Evento pedagógico registrado: {} ({})", evento.tipo(), evento.eventoId());
        } else {
            LOG.info("Evento pedagógico já registrado: {}", evento.eventoId());
        }
    }

    private DadosHistorico extrairDados(EventoAcademico evento) {
        if (TipoEvento.AVALIACAO_ENVIADA.getTipo().equals(evento.tipo())) {
            AvaliacaoEnviadaDados dados = jsonMapper.convertValue(
                evento.dados(), AvaliacaoEnviadaDados.class
            );
            validarIdentificadores(dados.alunoId(), dados.avaliacaoId());
            return new DadosHistorico(dados.alunoId(), dados.avaliacaoId(), null);
        }

        if (TipoEvento.AVALIACAO_REVISADA.getTipo().equals(evento.tipo())) {
            AvaliacaoRevisadaDados dados = jsonMapper.convertValue(
                evento.dados(), AvaliacaoRevisadaDados.class
            );
            validarIdentificadores(dados.alunoId(), dados.avaliacaoId());
            return new DadosHistorico(dados.alunoId(), dados.avaliacaoId(), dados.nota());
        }

        return new DadosHistorico(null, null, null);
    }

    private void validarIdentificadores(Long alunoId, Long avaliacaoId) {
        if (alunoId == null || alunoId <= 0 || avaliacaoId == null || avaliacaoId <= 0) {
            throw new IllegalArgumentException("Aluno e avaliação devem ser informados");
        }
    }

    private record DadosHistorico(Long alunoId, Long avaliacaoId, BigDecimal nota) {
    }
}