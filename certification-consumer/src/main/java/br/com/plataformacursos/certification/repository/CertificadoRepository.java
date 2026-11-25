package br.com.plataformacursos.certification.repository;

import br.com.plataformacursos.certification.model.Certificado;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CertificadoRepository extends Repository<Certificado, UUID> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO certificados (evento_id, aluno_id, curso_id, carga_horaria)
        VALUES (:eventoId, :alunoId, :cursoId, :cargaHoraria)
        ON CONFLICT (evento_id) DO NOTHING
        """, nativeQuery = true)
    int inserirSeAusente(
        @Param("eventoId") UUID eventoId,
        @Param("alunoId") Long alunoId,
        @Param("cursoId") Long cursoId,
        @Param("cargaHoraria") Integer cargaHoraria
    );
}