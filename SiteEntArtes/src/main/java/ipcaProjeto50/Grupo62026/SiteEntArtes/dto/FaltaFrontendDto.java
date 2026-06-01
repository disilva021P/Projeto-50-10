package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

public record FaltaFrontendDto(
        String id,
        AulaTituloDto aula,
        UtilizadoreResumoDto utilizadorId,
        Boolean justificado,
        String motivo,
        String estado,
        UtilizadoreResumoDto marcardo_por,
        LocalDateTime criadoEm,
        LocalDateTime justificadoEm
) {}