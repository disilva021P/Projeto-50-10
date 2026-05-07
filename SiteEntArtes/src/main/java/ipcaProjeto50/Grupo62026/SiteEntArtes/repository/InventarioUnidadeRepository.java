package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioUnidadeRepository extends JpaRepository<InventarioUnidade, Integer> {

    @Query("""
        SELECT u FROM InventarioUnidade u
        JOIN FETCH u.estado
        WHERE (:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        AND (:estadoId IS NULL OR u.estado.id = :estadoId)
    """)
    Page<InventarioUnidade> filtrarInventario(
            @Param("nome") String nome,
            @Param("estadoId") Integer estadoId,
            @Param("tamanho") String tamanho,
            @Param("cor") String cor,
            @Param("condicao") String condicao,
            Pageable pageable
    );
}