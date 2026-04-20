package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstudioRepository extends JpaRepository<Estudio, Integer> {
    @Query("SELECT e FROM Estudio e " +
            "JOIN EstudioModalidade em ON em.estudio.id = e.id " +
            "LEFT JOIN Aula a ON a.estudio.id = e.id " +
            "WHERE em.modalidade.id = :modalidadeId " +
            "GROUP BY e.id, e.nome, e.capacidade " +
            "ORDER BY COUNT(a) ASC")
    List<Estudio> findEstudiosMenosOcupados(@Param("modalidadeId") Integer modalidadeId);
}