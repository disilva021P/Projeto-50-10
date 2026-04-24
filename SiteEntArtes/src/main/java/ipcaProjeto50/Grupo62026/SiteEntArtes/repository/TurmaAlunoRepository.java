package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TurmaAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TurmaAlunoId;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AlunoTurmaService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurmaAlunoRepository extends JpaRepository<TurmaAluno, TurmaAlunoId> {
    List<TurmaAluno> findAllByTurma_Id(Integer id);
}