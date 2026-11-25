package br.com.plataformacursos.producer.config;

import br.com.plataformacursos.shared.evento.TipoEvento;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_EVENTOS = "eventos.cursos";

    @Bean
    public Declarables topologiaRabbitMq() {
        TopicExchange exchange = new TopicExchange(EXCHANGE_EVENTOS, true, false);

        Queue certificacao = QueueBuilder.durable("fila.certificacao").build();
        Queue comunicacao = QueueBuilder.durable("fila.comunicacao").build();
        Queue pedagogico = QueueBuilder.durable("fila.pedagogico").build();
        Queue auditoria = QueueBuilder.durable("fila.auditoria").build();
        Queue indicadores = QueueBuilder.durable("fila.indicadores").build();

        String cursoConcluido = TipoEvento.CURSO_CONCLUIDO.getRoutingKey();
        String cursoAbandonado = TipoEvento.CURSO_ABANDONADO.getRoutingKey();
        String avaliacaoRevisada = TipoEvento.AVALIACAO_REVISADA.getRoutingKey();

        return new Declarables(
            exchange,
            certificacao,
            comunicacao,
            pedagogico,
            auditoria,
            indicadores,

            BindingBuilder.bind(certificacao).to(exchange).with(cursoConcluido),

            BindingBuilder.bind(comunicacao).to(exchange).with(cursoConcluido),
            BindingBuilder.bind(comunicacao).to(exchange).with(cursoAbandonado),
            BindingBuilder.bind(comunicacao).to(exchange).with(avaliacaoRevisada),

            BindingBuilder.bind(pedagogico).to(exchange).with("avaliacao.#"),
            BindingBuilder.bind(auditoria).to(exchange).with("#"),
            BindingBuilder.bind(indicadores).to(exchange).with(cursoConcluido)
        );
    }

    @Bean
    public ApplicationRunner verificarRabbitMq(RabbitTemplate rabbitTemplate) {
        return args -> rabbitTemplate.execute(
            canal -> canal.exchangeDeclarePassive(EXCHANGE_EVENTOS)
        );
    }
}