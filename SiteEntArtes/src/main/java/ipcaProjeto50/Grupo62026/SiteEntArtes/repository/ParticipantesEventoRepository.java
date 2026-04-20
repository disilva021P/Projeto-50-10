package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ParticipantesEvento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ParticipantesEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantesEventoRepository extends JpaRepository<ParticipantesEvento, Integer> {

    List<ParticipantesEvento> findByEventoId(Integer eventoId);

    @Query("SELECT pe FROM ParticipantesEvento pe WHERE pe.utilizador.id = :utilizadorId")
    List<ParticipantesEvento> findByUtilizadorId(@Param("utilizadorId") Integer utilizadorId);

    boolean existsByEventoIdAndUtilizadorId(Integer eventoId, Integer utilizadorId);

    void deleteByEventoIdAndUtilizadorId(Integer eventoId, Integer utilizadorId);
}