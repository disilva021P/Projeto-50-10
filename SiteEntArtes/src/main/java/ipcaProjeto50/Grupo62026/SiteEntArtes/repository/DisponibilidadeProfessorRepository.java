package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.DisponibilidadeProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadeProfessorRepository extends JpaRepository<DisponibilidadeProfessor, Integer> {
    List<DisponibilidadeProfessor> findAllByProfessor_Id(Integer id);

    @Query("SELECT d FROM DisponibilidadeProfessor d " +
            "WHERE d.professor.id = :professorId " +
            "AND d.diaSemana = :diaSemana " +
            "AND d.horaInicio <= :horaInicio " +
            "AND d.horaFim >= :horaFim " +  // Fim do que existe depois do início do novo
            "AND d.validoDe <= :data " +
            "AND d.validoAte >= :data")
    Optional<DisponibilidadeProfessor> verificarDisponibilidade(
            @Param("professorId") Integer professorId,
            @Param("diaSemana") Integer diaSemana,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}