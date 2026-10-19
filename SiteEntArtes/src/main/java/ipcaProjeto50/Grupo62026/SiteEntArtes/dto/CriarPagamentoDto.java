package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarPagamentoDto(
        BigDecimal valorPagamento,
        String descricao,
        String idUtilizador,      // hash do utilizador
        String idTipoPagamento,   // hash do tipo
        String idAula,            // hash da aula, pode ser null
        LocalDate dataPagamento
) {}
