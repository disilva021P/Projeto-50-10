package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula}
 */

public record AulaDto(
        String id, EstudioDto estudio, Integer duracaoMinutos,
        LocalDate dataAula, LocalTime horaInicio, LocalTime horaFim,
        UtilizadoreResumoDto criadoPo, HorarioTurmaDto idHorario) implements Serializable {
}