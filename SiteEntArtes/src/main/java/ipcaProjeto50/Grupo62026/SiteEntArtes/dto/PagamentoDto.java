package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoPagamento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento}
 */
public record PagamentoDto(String id, BigDecimal valorPagamento, Boolean pago, String descricao,
                           String idTipoPagamento,String tipoPagamentoNome , Aula aula, LocalDate dataPagamento,
                           LocalDate dataConfirmado, UtilizadoreResumoDto utilizadoreResumoDto) implements Serializable {
}