package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensagensGrupoRepository extends JpaRepository<MensagensGrupo, Integer> {

    // Busca o histórico completo de um grupo (usado ao abrir a conversa)
    List<MensagensGrupo> findByGrupoIdOrderByEnviadaEmAsc(Integer grupoId);

    // Busca apenas a última mensagem de um grupo específico (usado na nova lógica de preview)
    Optional<MensagensGrupo> findFirstByGrupoIdOrderByEnviadaEmDesc(Integer grupoId);

    /**
     * Esta query busca a última mensagem de cada grupo onde o user é membro.
     */
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