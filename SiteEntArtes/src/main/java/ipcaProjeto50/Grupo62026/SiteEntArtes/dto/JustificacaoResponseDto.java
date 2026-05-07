package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.Instant;

public record JustificacaoResponseDto(
        String id,
        String cancelamentoId,
        UtilizadoreResumoDto submetidoPor,
        Boolean aceite,
        Instant criadoEm
) {}