package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Notificacoe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacoeRepository extends JpaRepository<Notificacoe, Integer> {
    List<Notificacoe> findAllByDestinatarioIdOrderByCriadaEmDesc(Integer destinatarioId);
    Page<Notificacoe> findAllByDestinatarioId(Integer destinatarioId, Pageable pageable);

    Page<Notificacoe> findAllByDestinatarioIdAndLidaFalse(Integer destinatarioId, Pageable pageable);
}