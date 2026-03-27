package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MensagenRepository extends JpaRepository<Mensagen, Integer> {
    List<Mensagen> findAllByRemetenteIdOrDestinatarioIdOrderByEnviadaEmDesc(Integer remetenteId, Integer destinatarioId);
    @Query("SELECT m FROM Mensagen m WHERE " +
            "(m.remetente.id = :id1 AND m.destinatario.id = :id2) OR " +
            "(m.remetente.id = :id2 AND m.destinatario.id = :id1) " +
            "ORDER BY m.enviadaEm asc")
    List<Mensagen> findChatHistory(Integer id1, Integer id2);
}