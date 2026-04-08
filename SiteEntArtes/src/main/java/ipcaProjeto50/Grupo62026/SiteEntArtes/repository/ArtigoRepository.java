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

    // Mantém a lógica de procurar por estado da unidade (ex: Publicitado)
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

    // Listagem simples
    Page<Artigo> findByArquivadoFalse(Pageable pageable);

    /**
     * Filtro mestre do Marketplace:
     * Suporta Tipo de Negócio, Tamanho, Cor, Condição (ENUM) e Intervalo de Preço.
     */
    @Query("SELECT a FROM Artigo a WHERE a.arquivado = false " +
            "AND (:tipo IS NULL OR a.tipoNegocio = :tipo) " +
            "AND (:tam IS NULL OR a.tamanho = :tam) " +
            "AND (:cor IS NULL OR a.cor = :cor) " +
            "AND (:cond IS NULL OR a.condicao = :cond) " +
            "AND (:pMin IS NULL OR a.preco >= :pMin) " +
            "AND (:pMax IS NULL OR a.preco <= :pMax)")
    Page<Artigo> filtrarMarketplace(
            @Param("tipo") Integer tipoNegocio,
            @Param("tam")  String tamanho,
            @Param("cor")  String cor,
            @Param("cond") String condicao,
            @Param("pMin") Double precoMin,
            @Param("pMax") Double precoMax,
            Pageable pageable
    );
}