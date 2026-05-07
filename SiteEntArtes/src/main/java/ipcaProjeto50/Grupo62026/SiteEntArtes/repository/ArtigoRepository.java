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

    @Query("""
        SELECT a FROM Artigo a 
        WHERE a.arquivado = false 
        AND a.aprovado = true
        AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) 
        AND (:tipoId IS NULL OR 
            (:tipoId = 0 AND a.isDoacao = true) OR 
            (:tipoId = 1 AND a.isVenda = true) OR 
            (:tipoId = 2 AND a.isAluguer = true)) 
        AND (:tam IS NULL OR a.tamanho = :tam) 
        AND (:cor IS NULL OR a.cor = :cor) 
        AND (:cond IS NULL OR a.condicao = :cond) 
        AND (:pMin IS NULL OR 
            (a.isVenda = true AND a.precoVenda >= :pMin) OR 
            (a.isAluguer = true AND a.precoAluguer >= :pMin) OR 
            a.isDoacao = true) 
        AND (:pMax IS NULL OR 
            (a.isVenda = true AND a.precoVenda <= :pMax) OR 
            (a.isAluguer = true AND a.precoAluguer <= :pMax) OR 
            a.isDoacao = true) 
        AND (:donoId IS NULL OR a.donoUtilizador.id = :donoId)
    """)
    Page<Artigo> filtrarMarketplace(
            @Param("nome") String nome,
            @Param("tipoId") Integer tipoId,
            @Param("tam") String tam,
            @Param("cor") String cor,
            @Param("cond") String cond,
            @Param("pMin") Double pMin,
            @Param("pMax") Double pMax,
            @Param("donoId") Integer donoId,
            Pageable pageable
    );

    @Query("""
        SELECT a FROM Artigo a 
        WHERE a.arquivado = false 
        AND a.aprovado = false 
        AND a.isDoacao = true
    """)
    List<Artigo> findPendentesParaCoordenacao();

    @Query("""
        SELECT a FROM Artigo a
        WHERE a.arquivado = false
    """)
    Page<Artigo> findByArquivadoFalseAndEstadoUnidade(
            @Param("estadoId") Integer estadoId, Pageable pageable);

    Page<Artigo> findByArquivadoFalse(Pageable pageable);
}