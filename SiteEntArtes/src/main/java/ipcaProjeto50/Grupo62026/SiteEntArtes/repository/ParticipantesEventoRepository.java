package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ParticipantesEvento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ParticipantesEventoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantesEventoRepository extends JpaRepository<ParticipantesEvento, ParticipantesEventoId> {
}