package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtigoRepository extends JpaRepository<Artigo, Integer> {

    /**
     * Filtro mestre do Marketplace:
     */
    @Query("""
        SELECT DISTINCT a FROM Artigo a
        JOIN a.unidades u
        WHERE a.arquivado = false
        AND u.estado.id = 2
        AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%')))

        AND (:tipoId IS NULL 
             OR (:tipoId = 0 AND a.isDoacao = true)
             OR (:tipoId = 1 AND a.isVenda = true)
             OR (:tipoId = 2 AND a.isAluguer = true))
             
        AND (:tam IS NULL OR a.tamanho = :tam) 
        AND (:cor IS NULL OR a.cor = :cor) 
        AND (:cond IS NULL OR a.condicao = :cond) 
        AND (:pMin IS NULL OR (a.isVenda = true AND a.precoVenda >= :pMin) OR (a.isAluguer = true AND a.precoAluguer >= :pMin) OR a.isDoacao = true)
        AND (:pMax IS NULL OR (a.isVenda = true AND a.precoVenda <= :pMax) OR (a.isAluguer = true AND a.precoAluguer <= :pMax) OR a.isDoacao = true)
        
        AND (:donoId IS NULL OR a.donoUtilizador.id = :donoId)
    """)
    Page<Artigo> filtrarMarketplace(
            @Param("nome") String nome,
            @Param("tipoId") Integer tipoId, // Mudamos de tipoNegocio para tipoId (0, 1 ou 2)
            @Param("tam")  String tamanho,
            @Param("cor")  String cor,
            @Param("cond") String condicao,
            @Param("pMin") Double precoMin,
            @Param("pMax") Double precoMax,
            @Param("donoId") Integer donoId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT a FROM Artigo a 
        JOIN a.unidades u 
        WHERE a.arquivado = false 
        AND u.estado.id = 8
    """)
    List<Artigo> findPendentesParaCoordenacao();

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

    Page<Artigo> findByArquivadoFalse(Pageable pageable);
}