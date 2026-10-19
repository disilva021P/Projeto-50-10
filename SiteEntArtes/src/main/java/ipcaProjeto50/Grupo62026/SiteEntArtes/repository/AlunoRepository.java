package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlunoRepository extends JpaRepository<Aluno, Integer> {
    List<Aluno> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
    List<Aluno> findAllByAtivoTrue();
}