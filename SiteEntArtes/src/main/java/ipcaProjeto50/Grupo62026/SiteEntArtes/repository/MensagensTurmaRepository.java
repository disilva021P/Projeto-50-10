package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensTurma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensagensTurmaRepository extends JpaRepository<MensagensTurma,Integer> {
    @Query("SELECT m FROM MensagensTurma m " +
            "JOIN TurmaAluno ta ON m.turma.id = ta.id.turmaId " +
            "WHERE ta.id.alunoId = :alunoId " +
            "ORDER BY m.enviadaEm DESC")
    List<MensagensTurma> findMessagesByAlunoTurma(@Param("alunoId") Integer alunoId);
}