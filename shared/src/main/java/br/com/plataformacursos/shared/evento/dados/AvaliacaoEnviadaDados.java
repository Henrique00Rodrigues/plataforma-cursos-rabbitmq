package br.com.plataformacursos.shared.evento.dados;

public record AvaliacaoEnviadaDados(
    Long alunoId,
    Long cursoId,
    Long avaliacaoId
) {}
