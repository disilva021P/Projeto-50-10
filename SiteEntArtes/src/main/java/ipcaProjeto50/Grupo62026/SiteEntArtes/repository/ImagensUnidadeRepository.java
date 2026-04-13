package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ImagensUnidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImagensUnidadeRepository extends JpaRepository<ImagensUnidade, Integer> {

    // Adiciona esta linha para permitir procurar todas as imagens de uma unidade
    List<ImagensUnidade> findByUnidadeId(Integer unidadeId);

    // Este tu já deves ter ou estar a usar algo semelhante para a miniatura
    Optional<ImagensUnidade> findFirstByUnidadeId(Integer unidadeId);
}