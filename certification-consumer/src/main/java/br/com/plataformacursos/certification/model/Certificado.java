package br.com.plataformacursos.certification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificados")
public class Certificado {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "aluno_id")
    private Long alunoId;

    @Column(name = "curso_id")
    private Long cursoId;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "emitido_em")
    private Instant emitidoEm;

    protected Certificado() {
    }
}