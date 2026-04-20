package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlunoEstatisiticaDto {

    private BigDecimal totalPago;
    private BigDecimal totalPendente;
    private List<PagamentoDto> historico;

}
