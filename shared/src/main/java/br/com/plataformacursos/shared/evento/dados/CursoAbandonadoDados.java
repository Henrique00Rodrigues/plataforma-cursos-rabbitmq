package br.com.plataformacursos.shared.evento.dados;

public record CursoAbandonadoDados(
    Long alunoId,
    Long cursoId,
    String motivo
) {}
