package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Cancelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancelamentoRepository extends JpaRepository<Cancelamento, Integer> {

    // Todas as faltas de uma aula específica
    List<Cancelamento> findByAula_Id(Integer aulaId);

    // Todas as faltas de um utilizador específico
    List<Cancelamento> findByUtilizador_Id(Integer utilizadorId);

    // Faltas justificadas ou não de um utilizador
    List<Cancelamento> findByUtilizador_IdAndJustificado(Integer utilizadorId, Boolean justificado);

    // Contar faltas injustificadas de um utilizador
    long countByUtilizador_IdAndJustificado(Integer utilizadorId, Boolean justificado);
}