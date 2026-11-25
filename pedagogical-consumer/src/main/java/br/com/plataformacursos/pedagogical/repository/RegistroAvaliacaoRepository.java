package br.com.plataformacursos.pedagogical.repository;

import br.com.plataformacursos.pedagogical.model.RegistroAvaliacao;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RegistroAvaliacaoRepository extends Repository<RegistroAvaliacao, UUID> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO historico_avaliacoes (
            evento_id, tipo_evento, aluno_id, avaliacao_id, nota, ocorrido_em, payload
        )
        VALUES (
            :eventoId, :tipoEvento, :alunoId, :avaliacaoId, :nota, :ocorridoEm, :payload
        )
        ON CONFLICT (evento_id) DO NOTHING
        """, nativeQuery = true)
    int inserirSeAusente(
        @Param("eventoId") UUID eventoId,
        @Param("tipoEvento") String tipoEvento,
        @Param("alunoId") Long alunoId,
        @Param("avaliacaoId") Long avaliacaoId,
        @Param("nota") BigDecimal nota,
        @Param("ocorridoEm") Instant ocorridoEm,
        @Param("payload") String payload
    );
}