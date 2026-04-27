package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Cancelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CancelamentoRepository extends JpaRepository<Cancelamento, Integer> {
    // Listar faltas de um aluno específico (para o Encarregado)
    List<Cancelamento> findAllByUtilizador_Id(Integer utilizadorId);
    // Query para contar o total de faltas do aluno
    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId")
    long countTotalFaltas(@Param("alunoId") Integer alunoId);

    // Query para contar apenas as justificadas
    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId AND c.justificado = true")
    long countJustificadas(@Param("alunoId") Integer alunoId);

    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId AND c.justificado = false AND c.justificadoEm IS NULL")
    long countPendentes(@Param("alunoId") Integer alunoId);
    // Query para contar as pendentes (Não justificadas e dentro do prazo - ex: últimos 5 dias)
    // Para simplificar, vamos contar apenas onde justificado é false
    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId AND c.justificado = false AND c.justificadoEm IS NOT NULL")
    long countNaoJustificadas(@Param("alunoId") Integer alunoId);


    // Listar apenas o que falta validar (para a Coordenação)
    List<Cancelamento> findByJustificadoFalseAndJustificadoEmNull();

    // Query 1
    @Query("SELECT c FROM Cancelamento c " +
            "JOIN c.aula a, AulaProfessore ap " +
            "WHERE ap.aula.id = a.id " +
            "AND ap.professor.id = :professorIdHash")
    List<Cancelamento> findFaltasByProfessor(@Param("professorIdHash") Integer professorIdHash);

    // Query 2
    @Query("SELECT c FROM Cancelamento c " +
            "JOIN c.aula a, AulaProfessore ap " +
            "WHERE ap.aula.id = a.id " +
            "AND ap.professor.id = :decode " +
            "AND a.id = :decode1")
    List<Cancelamento> findFaltasByProfessorAula(@Param("decode") Integer decode, @Param("decode1") Integer decode1);

    // No CancelamentoRepository.java
    @Query("SELECT c FROM Cancelamento c WHERE c.utilizador.id IN :educandosIds")
    List<Cancelamento> findByUtilizadorIdIn(@Param("educandosIds") List<Integer> educandosIds);
}