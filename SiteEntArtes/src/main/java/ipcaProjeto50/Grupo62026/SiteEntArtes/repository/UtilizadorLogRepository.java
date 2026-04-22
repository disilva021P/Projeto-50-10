package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.UtilizadorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface UtilizadorLogRepository extends JpaRepository<UtilizadorLog, Integer> {
    @Query("SELECT COUNT(l) FROM UtilizadorLog l WHERE l.enderecoIp = :ip " +
            "AND l.sucesso = 0 AND l.ultimoLogin > :threshold")
    long countFailuresByIp(String ip, LocalDateTime threshold);
    @Query("SELECT COUNT(l) FROM UtilizadorLog l WHERE l.idUtilizador = :id " +
            "AND l.sucesso = 0 AND l.ultimoLogin > :threshold")
    long countRecentFailures(Integer id, LocalDateTime threshold);
}