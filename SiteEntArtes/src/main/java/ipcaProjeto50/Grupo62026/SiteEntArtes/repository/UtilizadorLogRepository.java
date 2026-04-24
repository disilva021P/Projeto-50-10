package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.UtilizadorLog;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UtilizadorLogRepository extends JpaRepository<UtilizadorLog, Integer> {
    @Query("SELECT COUNT(l) FROM UtilizadorLog l WHERE l.enderecoIp = :ip " +
            "AND l.sucesso = 0 AND l.ultimoLogin > :threshold")
    long countFailuresByIp(
            @Param("ip") String ip,
            @Param("threshold") LocalDateTime threshold
    );
}