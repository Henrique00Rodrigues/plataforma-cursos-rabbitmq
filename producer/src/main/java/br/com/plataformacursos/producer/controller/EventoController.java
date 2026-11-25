package br.com.plataformacursos.producer.controller;

import br.com.plataformacursos.shared.evento.dados.AvaliacaoEnviadaDados;
import br.com.plataformacursos.shared.evento.dados.AvaliacaoRevisadaDados;
import br.com.plataformacursos.shared.evento.dados.CursoAbandonadoDados;
import java.math.BigDecimal;

import br.com.plataformacursos.producer.service.EventoPublisher;
import br.com.plataformacursos.shared.evento.EventoAcademico;
import br.com.plataformacursos.shared.evento.TipoEvento;
import br.com.plataformacursos.shared.evento.dados.CursoConcluidoDados;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoPublisher eventoPublisher;

    public EventoController(EventoPublisher eventoPublisher) {
        this.eventoPublisher = eventoPublisher;
    }

    @PostMapping("/curso-concluido")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventoAcademico cursoConcluido(
        @Valid @RequestBody CursoConcluidoRequest requisicao
    ) {
        CursoConcluidoDados dados = new CursoConcluidoDados(
            requisicao.alunoId(),
            requisicao.cursoId(),
            requisicao.cargaHoraria()
        );

        return eventoPublisher.publicar(TipoEvento.CURSO_CONCLUIDO, dados);
    }

    public record CursoConcluidoRequest(
        @NotNull @Positive Long alunoId,
        @NotNull @Positive Long cursoId,
        @NotNull @Positive Integer cargaHoraria
    ) {}

        @PostMapping("/curso-abandonado")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventoAcademico cursoAbandonado(
        @Valid @RequestBody CursoAbandonadoRequest requisicao
    ) {
        CursoAbandonadoDados dados = new CursoAbandonadoDados(
            requisicao.alunoId(),
            requisicao.cursoId(),
            requisicao.motivo()
        );

        return eventoPublisher.publicar(TipoEvento.CURSO_ABANDONADO, dados);
    }

    @PostMapping("/avaliacao-enviada")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventoAcademico avaliacaoEnviada(
        @Valid @RequestBody AvaliacaoEnviadaRequest requisicao
    ) {
        AvaliacaoEnviadaDados dados = new AvaliacaoEnviadaDados(
            requisicao.alunoId(),
            requisicao.cursoId(),
            requisicao.avaliacaoId()
        );

        return eventoPublisher.publicar(TipoEvento.AVALIACAO_ENVIADA, dados);
    }

    @PostMapping("/avaliacao-revisada")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventoAcademico avaliacaoRevisada(
        @Valid @RequestBody AvaliacaoRevisadaRequest requisicao
    ) {
        AvaliacaoRevisadaDados dados = new AvaliacaoRevisadaDados(
            requisicao.alunoId(),
            requisicao.cursoId(),
            requisicao.avaliacaoId(),
            requisicao.nota()
        );

        return eventoPublisher.publicar(TipoEvento.AVALIACAO_REVISADA, dados);
    }

    @PostMapping("/{eventoId}/republicar")
    public ResponseEntity<Void> republicar(@PathVariable("eventoId") UUID eventoId) {
    if (eventoPublisher.republicar(eventoId)) {
        return ResponseEntity.accepted().build();
    }

    return ResponseEntity.notFound().build();
    }

    public record CursoAbandonadoRequest(
        @NotNull @Positive Long alunoId,
        @NotNull @Positive Long cursoId,
        String motivo
    ) {}

    public record AvaliacaoEnviadaRequest(
        @NotNull @Positive Long alunoId,
        @NotNull @Positive Long cursoId,
        @NotNull @Positive Long avaliacaoId
    ) {}

    public record AvaliacaoRevisadaRequest(
        @NotNull @Positive Long alunoId,
        @NotNull @Positive Long cursoId,
        @NotNull @Positive Long avaliacaoId,
        BigDecimal nota
    ) {}
}