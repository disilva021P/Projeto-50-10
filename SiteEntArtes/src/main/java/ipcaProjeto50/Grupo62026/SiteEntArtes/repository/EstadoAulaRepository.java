package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoAulaRepository extends JpaRepository<EstadoAula, Integer> {
    EstadoAula findbyId(Integer id);

    }