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
     * Agora inclui a verificação obrigatória de que o estado da unidade deve ser Publicado (2).
     * Nota: Adicionei o JOIN com unidades para podermos filtrar pelo estado.
     */
    @Query("""
        SELECT DISTINCT a FROM Artigo a 
        JOIN a.unidades u 
        WHERE a.arquivado = false 
        AND u.estado.id = 2 
        AND (:tipo IS NULL OR a.tipoNegocio = :tipo) 
        AND (:tam IS NULL OR a.tamanho = :tam) 
        AND (:cor IS NULL OR a.cor = :cor) 
        AND (:cond IS NULL OR a.condicao = :cond) 
        AND (:pMin IS NULL OR a.preco >= :pMin) 
        AND (:pMax IS NULL OR a.preco <= :pMax) 
        AND (:donoId IS NULL OR a.donoUtilizador.id = :donoId)
    """)
    Page<Artigo> filtrarMarketplace(
            @Param("tipo") Integer tipoNegocio,
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