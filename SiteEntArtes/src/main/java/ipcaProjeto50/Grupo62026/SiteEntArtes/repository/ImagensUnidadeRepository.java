package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ImagensUnidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImagensUnidadeRepository extends JpaRepository<ImagensUnidade, Integer> {

    /**
     * Retorna a lista completa de imagens de uma unidade.
     * Útil para a galeria de detalhes.
     */
    List<ImagensUnidade> findByArtigoId(Integer artigoId);

    /**
     * Retorna apenas a primeira imagem encontrada.
     * Útil para miniaturas (thumbnails).
     */
    Optional<ImagensUnidade> findFirstByArtigoId(Integer artigoId);

    /**
     * Conta quantas imagens existem para uma determinada unidade.
     * Usado para a validação de "pelo menos 1 imagem" na edição.
     */
    long countByArtigoId(Integer artigoId);
}