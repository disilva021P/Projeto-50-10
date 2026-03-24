package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaAlunoRepository extends JpaRepository<AulaAluno, AulaAlunoId> {
}