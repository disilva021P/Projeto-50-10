package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtigoRepository extends JpaRepository<Artigo, Integer> {

    // Lista só os não arquivados
    Page<Artigo> findByArquivadoFalse(Pageable pageable);

    // Lista por tipo (1, 2 ou 3) e não arquivados
    Page<Artigo> findByArquivadoFalseAndParaVenda(Integer  paraVenda, Pageable pageable);
}