package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Integer> {
    Page<Aula> findAll(Pageable paginacao);

    List<Aula> findByDataAula(LocalDate data);

    @Query(value = "SELECT a.* FROM aulas a " +
            "JOIN aula_alunos al ON a.id = al.aula_id " +
            "WHERE al.aluno_id = :id AND a.data_aula = :data",
            nativeQuery = true)
    List<Aula> findByDataEAluno(
            @Param("data") LocalDate data,
            @Param("id") Integer id
    );

    @Query("SELECT a FROM Aula a " +
            "JOIN AulaAluno aa ON aa.aula.id = a.id " +
            "WHERE aa.aluno.id = :alunoId " +
            "AND a.dataAula BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataAula ASC, a.horaInicio ASC")
    List<Aula> buscarHorarioDoAluno(
            @Param("alunoId") Integer alunoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
    @Query("SELECT a FROM Aula a JOIN AulaAluno al WHERE al.aula.id = :aulaId AND al.aluno.id = :alunoId")
    Optional<Aula> findAulaByIdAndAlunoId(@Param("aulaId") Integer aulaId, @Param("alunoId") Integer alunoId);

    @Query("SELECT COUNT(a) > 0 FROM Aula a WHERE a.estudio.id = :estudioId " +
            "AND a.dataAula = :data " +
            "AND a.horaInicio < :horaFim " +
            "AND a.horaFim > :horaInicio")
    boolean existeConflitoNoEstudio(
            @Param("estudioId") Integer estudioId,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}