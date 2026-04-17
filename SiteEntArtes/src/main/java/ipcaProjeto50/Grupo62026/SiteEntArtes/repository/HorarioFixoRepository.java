package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.HorarioTurma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface HorarioFixoRepository extends JpaRepository<HorarioTurma,Integer> {
    Page<HorarioTurma> findAll(Pageable paginacao);

    List<HorarioTurma> findAllByIdturma_Id(Integer idturmaId);
    List<HorarioTurma> findAllByIdturma_IdIn(List<Integer> idsTurmas);
}
