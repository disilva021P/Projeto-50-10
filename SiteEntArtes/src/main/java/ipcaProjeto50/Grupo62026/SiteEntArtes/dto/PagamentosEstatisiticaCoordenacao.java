package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PagamentosEstatisiticaCoordenacao {

    BigDecimal getTotalPago;
    BigDecimal getTotalPorPagar;

    // Default method: calcula a diferença sem precisar de nova query ou DTO extra
    public BigDecimal getDiferenca() {
        BigDecimal pago = getTotalPago != null ? getTotalPago : BigDecimal.ZERO;
        BigDecimal divida = getTotalPorPagar != null ? getTotalPorPagar : BigDecimal.ZERO;
        return pago.subtract(divida);
    }
}