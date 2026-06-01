package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface AulaAlunoRepository extends JpaRepository<AulaAluno, AulaAlunoId> {
    long countByAulaId(Integer aulaId);

    List<AulaAluno> findByAula_Id(Integer realId);

    List<AulaAluno> findByAluno_Id(Integer realId);
    List<AulaAluno> findAllByAula_Id(Integer id);
    void deleteAllByAula_Id(Integer idReal);

    Optional<AulaAluno> findFirstByAula_Id(Integer aulaId);

    boolean existsByAula_IdAndAluno_Id(Integer aulaId, Integer alunoId);

}