package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensagensGrupoRepository extends JpaRepository<MensagensGrupo, Integer> {
    // Busca mensagens de um grupo específico, ordenadas
    List<MensagensGrupo> findByGrupoIdOrderByEnviadaEmAsc(Integer grupoId);

    // Query para o Preview: Busca a última mensagem de cada grupo onde o utilizador é membro
    @Query("SELECT mg FROM MensagensGrupo mg " +
            "WHERE mg.id IN ( " +
            "  SELECT MAX(m.id) FROM MensagensGrupo m " +
            "  JOIN m.grupo g " +
            "  JOIN g.membros memb " +
            "  WHERE memb.id = :utilizadorId " +
            "  GROUP BY g.id " +
            ") ORDER BY mg.enviadaEm DESC")
    List<MensagensGrupo> findUltimasMensagensPorMembro(@Param("utilizadorId") Integer utilizadorId);
}