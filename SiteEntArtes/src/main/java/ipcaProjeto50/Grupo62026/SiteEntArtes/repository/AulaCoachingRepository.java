package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaCoaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
    @Query("SELECT ac FROM AulaCoaching ac " +
            "JOIN Aula a ON ac.id = a.id " +
            "JOIN AulaAluno aa ON aa.aula.id = a.id " +
            "WHERE aa.aluno.id = :alunoId "+
            "AND a.dataAula BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataAula ASC, a.horaInicio ASC"
    )
    Page<AulaCoaching> buscarAulaCoachingPorAlunoSemana(@Param("alunoId") Integer alunoId,@Param("inicio") LocalDate inicio,
                                                        @Param("fim") LocalDate fim, Pageable pageable);

    @Query("SELECT ac FROM AulaCoaching ac " +
            "WHERE ac.estado.id = 3 " + // Filtra apenas aulas com estado ID 3
            "AND ac.dataAula BETWEEN :inicio AND :fim " +
            "AND (SELECT COUNT(aa) FROM AulaAluno aa WHERE aa.aula.id = ac.id) < ac.maxAlunos " +
            "AND NOT EXISTS (SELECT 1 FROM AulaAluno aa2 WHERE aa2.aula.id = ac.id AND aa2.aluno.id = :alunoId) " +
            "ORDER BY ac.dataAula ASC, ac.horaInicio ASC")
    Page<AulaCoaching> buscaAulasCoachingDisponiveis(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );
    @Query("SELECT ac FROM AulaCoaching ac " +
            "WHERE ac.estado.id = 3 AND ac.modalidade.id=:modalidadeId AND " + // Filtra apenas aulas com estado ID 3
            " ac.dataAula BETWEEN :inicio AND :fim " +
            "AND (SELECT COUNT(aa) FROM AulaAluno aa WHERE aa.aula.id = ac.id) < ac.maxAlunos " +
            "AND NOT EXISTS (SELECT 1 FROM AulaAluno aa2 WHERE aa2.aula.id = ac.id AND aa2.aluno.id = :alunoId) " +
            "ORDER BY ac.dataAula ASC, ac.horaInicio ASC")
    Page<AulaCoaching> buscaAulasCoachingDisponiveilPorModalidade(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("modalidadeId") Integer id,
            Pageable pageable
    );
}
