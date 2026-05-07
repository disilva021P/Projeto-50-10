package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaProfessorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaProfessore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaProfessoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    @Modifying
    @Query("DELETE FROM AulaProfessore ap WHERE ap.aula.id = :idAula")
    void deleteAllByAula_Id(@Param("idAula") Integer idAula);
    List<AulaProfessore> findAllByAulaId(Integer id);

    @Modifying // Essencial para DELETE ou UPDATE
    @Transactional
    @Query("DELETE FROM AulaProfessore ap WHERE ap.aula.id IN " +
            "(SELECT a.id FROM Aula a WHERE a.idHorario.id = :idHorario)")
    void deleteAllByHorarioId(@Param("idHorario") Integer idHorario);

    @Modifying
    @Transactional
    @Query("DELETE FROM AulaProfessore ap WHERE ap.aula.id IN " +
            "(SELECT a.id FROM Aula a WHERE a.idHorario.id = :idHorario AND a.dataAula >= :hoje)")
    void deleteFutureByHorarioId(@Param("idHorario") Integer idHorario, @Param("hoje") LocalDate hoje);
}