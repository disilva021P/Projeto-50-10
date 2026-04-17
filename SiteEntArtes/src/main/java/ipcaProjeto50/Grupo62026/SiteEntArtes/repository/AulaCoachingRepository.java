package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaCoaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AulaCoachingRepository extends JpaRepository<AulaCoaching,Integer> {
    @Query("SELECT ac FROM AulaCoaching ac " +
            "JOIN Aula a ON ac.id = a.id " +
            "JOIN AulaAluno aa ON aa.aula.id = a.id " +
            "WHERE aa.aluno.id = :alunoId")
    List<AulaCoaching> buscarAulaCoachingPorAluno(@Param("alunoId") Integer alunoId);
    @Query("SELECT ac FROM AulaCoaching ac " +
            "JOIN Aula a ON ac.id = a.id " +
            "JOIN AulaAluno aa ON aa.aula.id = a.id " +
            "WHERE aa.aluno.id = :alunoId")
    Page<AulaCoaching> buscarAulaCoachingPorAluno(@Param("alunoId") Integer alunoId, Pageable pageable);
    @Query("SELECT a FROM AulaCoaching a " +
            "JOIN AulaProfessore ap ON ap.aula.id = a.id " +
            "WHERE ap.professor.id = :idDecoded " +
            "AND a.estado.id = 2 " +
            "ORDER BY a.dataAula ASC")
    Page<AulaCoaching> buscarAulaCoachingPendentesPorProfessor(
            @Param("idDecoded") Integer idDecoded,
            Pageable pageable
    );
}
