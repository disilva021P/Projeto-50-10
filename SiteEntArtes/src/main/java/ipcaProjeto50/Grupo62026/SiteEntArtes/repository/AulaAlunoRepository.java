package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface AulaAlunoRepository extends JpaRepository<AulaAluno, AulaAlunoId> {
    long countByAulaId(Integer aulaId);

    List<AulaAluno> findByAula_Id(Integer realId);

    List<AulaAluno> findByAluno_Id(Integer realId);

    void deleteAllByAula_Id(Integer idReal);
}