package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DespesasEstatisticaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentosEstatisiticaCoordenacao;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessorEstatisticaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {
    @Query("SELECT new ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentosEstatisiticaCoordenacao(" +
            "SUM(CASE WHEN p.pago = true THEN p.valorPagamento ELSE 0 END), " +
            "SUM(CASE WHEN p.pago = false THEN p.valorPagamento ELSE 0 END)) " +
            "FROM Pagamento p")
    PagamentosEstatisiticaCoordenacao getEstatisticas();

    @Query("SELECT new ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DespesasEstatisticaDto(" +
            "SUM(CASE WHEN p.pago = true THEN p.valorPagamento ELSE 0 END), " +
            "SUM(CASE WHEN p.pago = false THEN p.valorPagamento ELSE 0 END)) " +
            "FROM Pagamento p " +
            "WHERE p.idTipoPagamento.id IN :idsDespesa AND p.idutilizador.tipo.id=1")

    DespesasEstatisticaDto getEstatisticasDespesas(@Param("idsDespesa") List<Integer> idsDespesa);

    @Query("SELECT new ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessorEstatisticaDto(" +
            "SUM(CASE WHEN p.pago = true THEN p.valorPagamento ELSE 0 END), " +
            "SUM(CASE WHEN p.pago = false THEN p.valorPagamento ELSE 0 END)) " +
            "FROM Pagamento p " +
            "WHERE p.idutilizador.id = :professorId " )

    ProfessorEstatisticaDto getEstatisticasProfessor(@Param("professorId") Integer professorId);

    @Query("SELECT p FROM Pagamento p WHERE MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    List<Pagamento> findByMesEAno(@Param("mes") int mes, @Param("ano") int ano);

    List<Pagamento> findAllByIdutilizador_Id(Integer id);

    @Query("SELECT COALESCE(SUM(p.valorPagamento), 0) FROM Pagamento p WHERE p.idutilizador.id = :id AND p.pago = true")
    BigDecimal somarPagoPorUtilizador(@Param("id") Integer id);

    @Query("SELECT COALESCE(SUM(p.valorPagamento), 0) FROM Pagamento p WHERE p.idutilizador.id = :id AND p.pago = false")
    BigDecimal somarPendentePorUtilizador(@Param("id") Integer id);
}