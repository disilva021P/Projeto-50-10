package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Integer> {

    Page<Evento> findAll(Pageable pageable);

    List<Evento> findByDataEventoAfterOrderByDataEventoAsc(LocalDate data);

    @Query("SELECT e FROM Evento e WHERE e.criadoPor.id = :utilizadorId ORDER BY e.dataEvento ASC")
    List<Evento> findByCriadoPorId(@Param("utilizadorId") Integer utilizadorId);
}