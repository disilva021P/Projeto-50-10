package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Notificacoe;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface NotificacoeRepository extends JpaRepository<Notificacoe, Integer> {
    List<Notificacoe> findAllByUtilizadorIdOrderByCriadaEmDesc(Integer utilizadorId);
    Page<Notificacoe> findAllByUtilizadorId(Integer utilizadorId, Pageable pageable);
}