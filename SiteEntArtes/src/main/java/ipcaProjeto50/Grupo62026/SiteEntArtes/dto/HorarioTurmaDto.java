package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstudioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.HorarioTurma}
 */
//TODO: MUDAR PARA TURMADTO
public record HorarioTurmaDto(UtilizadoreResumoDto criadoPor, Turma idturma, LocalDate dataInicio, LocalDate dataValidade,
                              Integer diaSemana, Integer duracaoMinutos, LocalTime horaInicio,
                              LocalTime horaFim, EstudioDto estudioId) implements Serializable {
}