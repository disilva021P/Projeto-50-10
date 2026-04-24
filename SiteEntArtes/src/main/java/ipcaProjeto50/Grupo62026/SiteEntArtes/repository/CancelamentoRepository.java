package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Cancelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CancelamentoRepository extends JpaRepository<Cancelamento, Integer> {
    // Listar faltas de um aluno específico (para o Encarregado)
    List<Cancelamento> findByUtilizadorId(Integer utilizadorId);

    // Query para contar o total de faltas do aluno
    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId")
    long countTotalFaltas(@Param("alunoId") Integer alunoId);

    // Query para contar apenas as justificadas
    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId AND c.justificado = true")
    long countJustificadas(@Param("alunoId") Integer alunoId);

    // Query para contar as pendentes (Não justificadas e dentro do prazo - ex: últimos 5 dias)
    // Para simplificar, vamos contar apenas onde justificado é false
    @Query("SELECT COUNT(c) FROM Cancelamento c WHERE c.utilizador.id = :alunoId AND c.justificado = false")
    long countNaoJustificadas(@Param("alunoId") Integer alunoId);


    // Listar apenas o que falta validar (para a Coordenação)
    List<Cancelamento> findByJustificadoFalse();
}