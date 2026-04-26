package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EncarregadoAlunoRepository extends JpaRepository<EncarregadoAluno, EncarregadoAlunoId> {
    List<EncarregadoAluno> id(EncarregadoAlunoId id);

    List<EncarregadoAluno> findAllByEncarregado_Id(Integer idEducador);List<EncarregadoAluno> findAllByAluno_Id(Integer alunoId);


    Optional<EncarregadoAluno> findByEncarregado_IdAndAluno_Id(Integer idEncarregado, Integer idAluno);

    boolean existsByEncarregado_IdAndAluno_Id(Integer idEncarregado, Integer idAluno);



}