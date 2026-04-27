package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        String artigoId,
        String compradorId,
        String tipo, // "VENDA", "ALUGUER" ou "DOACAO"
        BigDecimal valorFinal,
        LocalDate dataInicio,
        LocalDate dataFimPrevista
) {}
