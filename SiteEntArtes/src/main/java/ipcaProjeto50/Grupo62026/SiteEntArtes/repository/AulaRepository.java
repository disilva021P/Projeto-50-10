package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

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
    );}
