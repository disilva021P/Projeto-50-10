package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.Instant;

public record CancelamentoResponseDto(
        String id,
        String aulaId,
        UtilizadoreResumoDto utilizador,
        String motivo,
        Boolean justificado,
        Instant criadoEm,
        Instant justificadoEm
){}