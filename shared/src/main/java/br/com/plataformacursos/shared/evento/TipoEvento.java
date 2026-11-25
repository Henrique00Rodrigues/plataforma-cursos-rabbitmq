package br.com.plataformacursos.shared.evento;

public enum TipoEvento {

    CURSO_CONCLUIDO("CursoConcluido", "curso.concluido"),
    CURSO_ABANDONADO("CursoAbandonado", "curso.abandonado"),
    AVALIACAO_ENVIADA("AvaliacaoEnviada", "avaliacao.enviada"),
    AVALIACAO_REVISADA("AvaliacaoRevisada", "avaliacao.revisada");

    private final String tipo;
    private final String routingKey;

    TipoEvento(String tipo, String routingKey) {
        this.tipo = tipo;
        this.routingKey = routingKey;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
