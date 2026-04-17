package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoAulaRepository extends JpaRepository<EstadoAula, Integer> {
        Optional<EstadoAula> findById(Integer id);

    }