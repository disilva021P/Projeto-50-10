package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResumoDto(
        String id,
        String artigoNome,
        String artigoDescricao,
        String artigoId,
        LocalDate dataInicio,
        LocalDate dataFimPrevista,
        BigDecimal valorFinal
) {}
