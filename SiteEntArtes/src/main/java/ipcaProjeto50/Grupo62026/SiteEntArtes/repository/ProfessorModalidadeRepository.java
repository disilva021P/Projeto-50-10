package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ProfessorModalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ProfessorModalidadeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfessorModalidadeRepository extends JpaRepository<ProfessorModalidade, ProfessorModalidadeId> {
    @Query("SELECT pm FROM ProfessorModalidade pm WHERE pm.modalidade.id = :modalidadeId")
    Page<ProfessorModalidade> findByModalidadeIdCustom(
            @Param("modalidadeId") Integer modalidadeId,
            Pageable pageable
    );
    boolean existsByModalidadeIdAndProfessorId(Integer modalidadeId, Integer professorId);
    List<ProfessorModalidade> findById_ProfessorId(Integer professorId);
    void deleteByProfessorId(Integer professorId);
}