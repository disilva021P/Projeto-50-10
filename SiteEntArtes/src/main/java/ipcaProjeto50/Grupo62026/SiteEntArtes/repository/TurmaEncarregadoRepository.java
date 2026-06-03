package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TurmaEncarregado;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TurmaEncarregadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurmaEncarregadoRepository extends JpaRepository<TurmaEncarregado, TurmaEncarregadoId> {
    void deleteByEncarregado_Id(Integer encarregadoId);
}