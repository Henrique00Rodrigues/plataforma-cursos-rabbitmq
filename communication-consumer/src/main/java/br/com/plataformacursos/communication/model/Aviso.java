package br.com.plataformacursos.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "avisos")
public class Aviso {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "tipo_evento")
    private String tipoEvento;

    @Column(name = "aluno_id")
    private Long alunoId;

    @Column(name = "curso_id")
    private Long cursoId;

    @Column(name = "avaliacao_id")
    private Long avaliacaoId;

    @Column(name = "texto")
    private String texto;

    @Column(name = "criado_em")
    private Instant criadoEm;

    protected Aviso() {
    }
}