package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DespesasEstatisticaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentosEstatisiticaCoordenacao;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessorEstatisticaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    @Query("SELECT new ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentosEstatisiticaCoordenacao(" +
            "SUM(CASE WHEN p.pago = true THEN p.valorPagamento ELSE 0 END), " +
            "SUM(CASE WHEN p.pago = false THEN p.valorPagamento ELSE 0 END)) " +
            "FROM Pagamento p "+
            "WHERE p.idTipoPagamento.id IN :idsReceita AND p.idutilizador.tipo.id<>1")
    PagamentosEstatisiticaCoordenacao getEstatisticas(@Param("idsReceita") List<Integer> idsReceita);

    @Query("SELECT new ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DespesasEstatisticaDto(" +
            "SUM(CASE WHEN p.pago = true THEN p.valorPagamento ELSE 0 END), " +
            "SUM(CASE WHEN p.pago = false THEN p.valorPagamento ELSE 0 END)) " +
            "FROM Pagamento p " +
            "WHERE p.idTipoPagamento.id IN :idsDespesa AND p.idutilizador.tipo.id=1")
    DespesasEstatisticaDto getEstatisticasDespesas(@Param("idsDespesa") List<Integer> idsDespesa);

    // --- ATUALIZADO: Estatística de Professor com filtro de Mês e Ano ---
    @Query("SELECT new ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessorEstatisticaDto(" +
            "SUM(CASE WHEN p.pago = true THEN p.valorPagamento ELSE 0 END), " +
            "SUM(CASE WHEN p.pago = false THEN p.valorPagamento ELSE 0 END)) " +
            "FROM Pagamento p " +
            "WHERE p.idutilizador.id = :professorId " +
            "AND MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    ProfessorEstatisticaDto getEstatisticasProfessor(
            @Param("professorId") Integer professorId,
            @Param("mes") int mes,
            @Param("ano") int ano);

    @Query("SELECT p FROM Pagamento p WHERE MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    List<Pagamento> findByMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    // --- QUERIES PARA FILTRO DE ALUNO ---

    @Query("SELECT COALESCE(SUM(p.valorPagamento), 0) FROM Pagamento p " +
            "WHERE p.idutilizador.id = :id AND p.pago = true " +
            "AND MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    BigDecimal somarPagoPorUtilizador(@Param("id") Integer id, @Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT COALESCE(SUM(p.valorPagamento), 0) FROM Pagamento p " +
            "WHERE p.idutilizador.id = :id AND p.pago = false " +
            "AND MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    BigDecimal somarPendentePorUtilizador(@Param("id") Integer id, @Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT p FROM Pagamento p " +
            "WHERE p.idutilizador.id = :id " +
            "AND MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    List<Pagamento> findAllByUtilizadorAndMesEAno(@Param("id") Integer id, @Param("mes") int mes, @Param("ano") int ano);

    List<Pagamento> findAllByIdutilizador_Id(Integer id);


    Page<Pagamento> findAllByIdutilizador_Id(Integer id, Pageable pageable);


}