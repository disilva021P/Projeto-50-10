package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagenRepository extends JpaRepository<Mensagen, Integer> {
    List<Mensagen> findAllByRemetenteIdOrDestinatarioIdOrderByEnviadaEmDesc(Integer remetenteId, Integer destinatarioId);}