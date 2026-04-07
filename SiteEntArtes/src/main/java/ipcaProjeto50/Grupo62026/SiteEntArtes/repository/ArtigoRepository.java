package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtigoRepository extends JpaRepository<Artigo, Integer> {

    // Todos os artigos não arquivados que tenham pelo menos uma unidade com o estado pretendido
    @Query("""
        SELECT DISTINCT a FROM Artigo a
        JOIN FETCH a.unidades u
        JOIN FETCH u.estado
        WHERE a.arquivado = false
        AND u.estado.id = :estadoId
        AND u.disponivel = true
    """)
    Page<Artigo> findByArquivadoFalseAndEstadoUnidade(
            @Param("estadoId") Integer estadoId, Pageable pageable);

    // Todos os artigos não arquivados (sem filtro de estado)
    Page<Artigo> findByArquivadoFalse(Pageable pageable);

    @Query("SELECT a FROM Artigo a WHERE a.arquivado = false " +
            "AND (:tipo IS NULL OR a.tipoNegocio = :tipo) " +
            "AND (:tam IS NULL OR a.tamanho = :tam) " +
            "AND (:pMin IS NULL OR a.preco >= :pMin) " +
            "AND (:pMax IS NULL OR a.preco <= :pMax)")
    Page<Artigo> filtrarMarketplace(
            @Param("tipo") Integer tipoNegocio,
            @Param("tam") String tamanho,
            @Param("pMin") Double precoMin,
            @Param("pMax") Double precoMax,
            Pageable pageable
    );
}