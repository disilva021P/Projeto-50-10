package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstudioModalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstudioModalidadeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstudioModalidadeRepository extends JpaRepository<EstudioModalidade, EstudioModalidadeId> {
    Optional<EstudioModalidade> findByEstudio_IdAndModalidade_Id(Integer id, Integer id1);
}