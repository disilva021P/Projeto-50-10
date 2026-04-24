package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Evento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ParticipantesEvento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ParticipantesEventoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ParticipantesEventoRepository extends JpaRepository<ParticipantesEvento, ParticipantesEventoId> {

    List<ParticipantesEvento> findByEventoId(Integer eventoId);

    @Query("SELECT pe FROM ParticipantesEvento pe WHERE pe.utilizador.id = :utilizadorId")
    List<ParticipantesEvento> findByUtilizadorId(@Param("utilizadorId") Integer utilizadorId);

    @Query("SELECT pe.evento FROM ParticipantesEvento pe WHERE pe.utilizador.id = :utilizadorId AND pe.cancelado = false")
    List<Evento> findEventosAtivosPorUtilizador(@Param("utilizadorId") Integer utilizadorId);

    boolean existsByEventoIdAndUtilizadorId(Integer eventoId, Integer utilizadorId);

    @Transactional
    @Modifying
    void deleteByEventoIdAndUtilizadorId(Integer eventoId, Integer utilizadorId);
}