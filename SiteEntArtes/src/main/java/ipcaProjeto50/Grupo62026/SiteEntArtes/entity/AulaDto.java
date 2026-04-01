package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link Aula}
 */
public record AulaDto(Estudio estudio, UtilizadoreResumoDto criadoPor, Integer duracaoMinutos, LocalDate dataAula,
                      LocalTime horaInicio, LocalTime horaFim, String notas) implements Serializable {
}