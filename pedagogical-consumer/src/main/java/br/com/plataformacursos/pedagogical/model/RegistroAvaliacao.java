package br.com.plataformacursos.pedagogical.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "historico_avaliacoes")
public class RegistroAvaliacao {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "tipo_evento")
    private String tipoEvento;

    @Column(name = "aluno_id")
    private Long alunoId;

    @Column(name = "avaliacao_id")
    private Long avaliacaoId;

    @Column(name = "nota")
    private BigDecimal nota;

    @Column(name = "ocorrido_em")
    private Instant ocorridoEm;

    @Column(name = "payload")
    private String payload;

    protected RegistroAvaliacao() {
    }
}