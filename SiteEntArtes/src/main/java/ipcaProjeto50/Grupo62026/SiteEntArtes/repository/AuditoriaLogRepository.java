package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AuditoriaLogDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AuditoriaLog;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Integer> {
     Page<AuditoriaLog> findAll(Pageable pageable);
}