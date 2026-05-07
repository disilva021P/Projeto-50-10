package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DespesasEstatisticaDto {
    private BigDecimal totalDespesaEfetiva; // O valor que aparece no card (€ 3.180)
    private BigDecimal totalDespesaPendente;
}