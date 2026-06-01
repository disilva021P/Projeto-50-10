package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Integer> {
    Page<Aula> findAll(Pageable paginacao);

    List<Aula> findByDataAula(LocalDate data);

    @Query("SELECT a FROM Aula a " +
            "JOIN AulaAluno aa ON a.id = aa.aula.id " +
            "WHERE aa.aluno.id = :id AND a.dataAula = :data")
    List<Aula> findByDataEAluno(
            @Param("data") LocalDate data,
            @Param("id") Integer id
    );

    @Query("SELECT DISTINCT a FROM Aula a " +
            "JOIN a.idHorario h " + // Assume que idHorario é o nome do atributo na entidade Aula
            "JOIN h.idturma t " +   // Assume que idturma é o objeto Turma no HorarioTurma
            "JOIN TurmaAluno ta ON ta.turma.id = t.id " + // Liga a inscrição à turma
            "WHERE ta.aluno.id = :alunoId " + // Filtra pelo aluno logado
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
    void deleteAllByIdHorario_Id(Integer id);
    List<Aula> findAllByIdHorario_Id(Integer id);

    @Query("SELECT a FROM Aula a WHERE a.estado.id not in (4,7,8,9) AND (a.dataAula < :dataLimite " +
            "OR (a.dataAula = :dataLimite AND a.horaFim <= :horaLimite))")
    List<Aula> findAulasPassadasHa48Horas(
            @Param("dataLimite") LocalDate dataLimite,
            @Param("horaLimite") LocalTime horaLimite
    );
    @Query("SELECT a FROM Aula a WHERE a.estado.id=3 AND (a.dataAula < :dataLimite " +
            "OR (a.dataAula = :dataLimite AND a.horaFim <= :horaLimite))")
    List<Aula> findAulasRealizadas(
            @Param("dataLimite") LocalDate dataLimite,
            @Param("horaLimite") LocalTime horaLimite
    );
    @Query("""
    SELECT a FROM Aula a
    JOIN AulaProfessore ap ON ap.aula.id = a.id
    WHERE ap.professor.id = :professorId
    AND a.estado.id=3
    AND a.dataAula BETWEEN :inicio AND :fim
""")
    List<Aula> findAulasByProfessorAndSemana(
            @Param("professorId") Integer professorId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
    // Encontra aulas futuras de um horário específico
    List<Aula> findAllByIdHorario_IdAndDataAulaAfter(Integer horarioId, LocalDate data);

    // Apaga aulas futuras de um horário específico
    @Modifying
    @Transactional
    void deleteByIdHorario_IdAndDataAulaAfter(Integer horarioId, LocalDate data);
    @Modifying

    @Transactional
    @Query("UPDATE Aula a SET a.horaInicio = :inicio, a.horaFim = :fim, a.estudio.id = :estId, a.duracaoMinutos = :duracao " +
            "WHERE a.idHorario.id = :horarioId AND a.dataAula >= :hoje")
    void updateAulasFuturas(
            @Param("horarioId") Integer horarioId,
            @Param("inicio") LocalTime inicio,
            @Param("fim") LocalTime fim,
            @Param("estId") Integer estId,
            @Param("duracao") Integer duracao,
            @Param("hoje") LocalDate hoje
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Aula a WHERE a.idHorario.id = :idHorario AND a.dataAula >= :hoje")
    void deleteFutureByHorarioId(@Param("idHorario") Integer idHorario, @Param("hoje") LocalDate hoje);

    @Query("SELECT em.estudio.id " +
            "FROM EstudioModalidade em " +
            "LEFT JOIN Aula a ON a.estudio.id = em.estudio.id AND a.dataAula = :data " +
            "WHERE em.modalidade.id = :modalidadeId " +
            "GROUP BY em.estudio.id " +
            "ORDER BY COUNT(a.id) ASC")
    List<Integer> findEstudiosPorModalidadeOrdenadosPorAulasDoDia(
            @Param("modalidadeId") Integer modalidadeId,
            @Param("data") LocalDate data
    );
    @Query("SELECT DISTINCT a FROM Aula a " +
            // Caminho 1: Aulas por Turma (Regulares)
            "LEFT JOIN a.idHorario h " +
            "LEFT JOIN h.idturma t " +
            "LEFT JOIN TurmaAluno ta ON ta.turma.id = t.id " +
            // Caminho 2: Aulas com marcação direta (Coaching / Privadas)
            "LEFT JOIN AulaAluno al ON al.aula.id = a.id " +
            // Filtros
            "WHERE (ta.aluno.id = :alunoId OR al.aluno.id = :alunoId) " +
            "AND a.dataAula BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataAula ASC, a.horaInicio ASC")
    List<Aula> buscarHorarioCompletoDoAluno(
            @Param("alunoId") Integer alunoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
    @Query("SELECT a FROM Aula a " +
            "JOIN AulaProfessore ap ON ap.aula.id = a.id " +
            "WHERE ap.professor.id = :id " +
            "AND (a.dataAula < :dataAtual OR (a.dataAula = :dataAtual AND a.horaFim < :horaAtual))")
    List<Aula> findAllProfessorPassadas(
            @Param("id") Integer id,
            @Param("dataAtual") LocalDate dataAtual,
            @Param("horaAtual") LocalTime horaAtual,
            Pageable pageable
    );
    @Query("SELECT aa.aluno FROM AulaAluno aa WHERE aa.aula.id = :aulaId")
    List<Aluno> findAlunosInscritosDiretamente(@Param("aulaId") Integer aulaId);

    // 2. Devolve todos os alunos da Turma associada a uma determinada Aula
    // (Útil para aulas de grupo/regulares onde os alunos pertencem à turma do horário)
    @Query("SELECT ta.aluno FROM TurmaAluno ta " +
            "JOIN Aula a ON a.idHorario.idturma.id = ta.turma.id " +
            "WHERE a.id = :aulaId")
    List<Aluno> findAlunosDaTurmaPorAula(@Param("aulaId") Integer aulaId);

    @Query("SELECT DISTINCT a FROM Aula a " +
            // Caminho 1: Alunos por inscrição direta
            "LEFT JOIN AulaAluno aa ON a.id = aa.aula.id " +
            // Caminho 2: Alunos por Turma
            "LEFT JOIN a.idHorario h " +
            "LEFT JOIN h.idturma t " +
            "LEFT JOIN TurmaAluno ta ON ta.turma.id = t.id " +
            // Caminho 3: Professores associados à aula
            "LEFT JOIN AulaProfessore ap ON ap.aula.id = a.id " +
            // Filtros: Traz a aula se a data bater E o ID for do aluno (direto/turma) OU se for o professor da aula
            "WHERE a.dataAula = :data " +
            "AND (aa.aluno.id = :id OR ta.aluno.id = :id OR ap.professor.id = :id)")
    List<Aula> findByDataEUtilizadorGeral(
            @Param("data") LocalDate data,
            @Param("id") Integer id
    );
}