package br.com.plataformacursos.shared.evento.dados;

public record CursoConcluidoDados(
    Long alunoId,
    Long cursoId,
    Integer cargaHoraria
) {}
