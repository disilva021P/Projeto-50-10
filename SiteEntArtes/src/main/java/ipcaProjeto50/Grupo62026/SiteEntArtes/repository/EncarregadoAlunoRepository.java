package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncarregadoAlunoRepository extends JpaRepository<EncarregadoAluno, EncarregadoAlunoId> {
    List<EncarregadoAluno> id(EncarregadoAlunoId id);

    List<EncarregadoAluno> findAllByEncarregado_Id(Integer idEducador);
}