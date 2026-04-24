package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaProfessorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaProfessore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaProfessoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface AulaProfessoreRepository extends JpaRepository<AulaProfessore, AulaProfessoreId> {
    @Query("SELECT COUNT(ap) > 0 FROM AulaProfessore ap WHERE ap.professor.id = :professorId " +
            "AND ap.aula.dataAula =:data " +
            "AND ap.aula.horaInicio < :horaFim " +
            "AND ap.aula.horaFim > :horaInicio AND ap.aula.estado.id>2"

    )
    boolean professorJaPossuiAula(
            @Param("professorId") Integer id,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );

    void deleteAllByAula_IdAndAula_Id(Integer aulaId, Integer aulaId1);

    List<AulaProfessore> findByAula_Id(Integer id);

    List<AulaProfessore> findByProfessor_Id(Integer professorId);
    boolean existsByAula_IdAndProfessor_Id(Integer id1, Integer id2);
    Optional<AulaProfessore> findByAula_IdAndProfessor_Id(Integer idAula, Integer idProfessor);
    void deleteAllByAula_Id(Integer aula_id);
    List<AulaProfessore> findAllByAulaId(Integer id);
}