package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ImagensUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImagensUnidadeRepository extends JpaRepository<ImagensUnidade, Integer> {
    Optional<ImagensUnidade> findFirstByUnidadeId(Integer unidadeId);
}