package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {

    @Query("SELECT t FROM Transacao t WHERE t.comprador.id = :compradorId AND t.tipo = 'ALUGUER' AND t.dataDevolucaoReal IS NULL")
    List<Transacao> findAlugueresAtivosByComprador(@Param("compradorId") Integer compradorId);
}

