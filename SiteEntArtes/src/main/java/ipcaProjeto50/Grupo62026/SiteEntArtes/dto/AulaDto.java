package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula}
 */

public record AulaDto(
        String id,
        Integer duracaoMinutos,
        LocalDate dataAula,
        LocalTime horaInicio,
        LocalTime horaFim,
        String notas,
        Integer idHorario
) {}