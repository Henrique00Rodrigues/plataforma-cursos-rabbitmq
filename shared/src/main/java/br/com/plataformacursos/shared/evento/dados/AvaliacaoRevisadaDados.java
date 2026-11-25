package br.com.plataformacursos.shared.evento.dados;

import java.math.BigDecimal;

public record AvaliacaoRevisadaDados(
    Long alunoId,
    Long cursoId,
    Long avaliacaoId,
    BigDecimal nota
) {}
