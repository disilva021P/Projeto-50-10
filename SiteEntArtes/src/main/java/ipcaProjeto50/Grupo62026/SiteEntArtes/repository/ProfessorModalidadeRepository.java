package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ProfessorModalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ProfessorModalidadeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorModalidadeRepository extends JpaRepository<ProfessorModalidade, ProfessorModalidadeId> {
    Page<ProfessorModalidade> findByModalidade_Id(Integer idReal, Pageable pageable);

    boolean existsByModalidadeIdAndProfessorId(Integer modalidadeId, Integer professorId);
}