package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.DisponibilidadeProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadeProfessorRepository extends JpaRepository<DisponibilidadeProfessor, Integer> {
    List<DisponibilidadeProfessor> findAllByProfessor_Id(Integer id);
    @Query("SELECT COUNT(ap) > 0 FROM AulaProfessore ap " +
            "JOIN ap.aula a " +
            "JOIN AulaCoaching ac ON ac.id = a.id " + // Join pelo ID partilhado
            "WHERE ap.professor.id = :professorId " +
            "AND a.dataAula = :data " +
            "AND a.horaInicio < :horaFim " +
            "AND a.horaFim > :horaInicio " +
            "AND ac.estado.id != 1 AND ac.estado.id != 2 AND ac.estado.id != 4") //ou seja, disponivel? pedido e agendado
    Optional<DisponibilidadeProfessor> verificarDisponibilidade(
            @Param("id") Integer id,
            @Param("diaSemana") Integer diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}