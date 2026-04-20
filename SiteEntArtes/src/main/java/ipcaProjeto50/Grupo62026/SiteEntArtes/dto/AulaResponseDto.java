package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;


import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaCoaching;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public record AulaResponseDto(
        String id, Boolean fixo, EstudioDto estudio, Integer duracaoMinutos,
        LocalDate dataAula, LocalTime horaInicio, LocalTime horaFim,
        String criadoPo, HorarioTurmaDto idHorario, EstadoAulaDto estado, AulaCoachingDto coachingDto) implements Serializable {
}