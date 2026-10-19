package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public record AulaTituloDto(
        String id, EstudioDto estudio, Integer duracaoMinutos,
        LocalDate dataAula, LocalTime horaInicio, LocalTime horaFim,
        String criadoPo, HorarioTurmaDto idHorario, EstadoAulaDto estado, String titulo,
        Integer maxAlunos,
        UtilizadoreResumoDto solicitadoPor
) implements Serializable {}
