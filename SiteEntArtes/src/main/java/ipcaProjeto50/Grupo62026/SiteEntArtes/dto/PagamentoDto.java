package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoPagamento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento}
 */
public record PagamentoDto(String id, BigDecimal valorPagamento, Boolean pago, String descricao,
                           String idTipoPagamento,String tipoPagamentoNome , AulaDto aula,
                            LocalDate dataPagamento,
                           LocalDate dataConfirmado, UtilizadoreResumoDto utilizadoreResumoDto) implements Serializable {
}