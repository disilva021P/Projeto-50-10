package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.HorarioTurma}
 */
public record HorarioTurmaDto(String id, UtilizadoreResumoDto criadoPor, TurmaDto idturma, LocalDate dataInicio, LocalDate dataValidade,
                              Integer diaSemana, Integer duracaoMinutos, LocalTime horaInicio,
                              LocalTime horaFim, EstudioDto estudioId) implements Serializable {

}