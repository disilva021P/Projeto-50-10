package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProfessorEstatisticaDto {
    private BigDecimal totalEsperado; // Já Recebido + Por Liquidar
    private BigDecimal jaRecebido;
    private BigDecimal porLiquidar;

    // Construtor customizado para facilitar a Query JPQL
    public ProfessorEstatisticaDto(BigDecimal jaRecebido, BigDecimal porLiquidar) {
        this.jaRecebido = (jaRecebido != null) ? jaRecebido : BigDecimal.ZERO;
        this.porLiquidar = (porLiquidar != null) ? porLiquidar : BigDecimal.ZERO;
        this.totalEsperado = this.jaRecebido.add(this.porLiquidar);
    }
}
