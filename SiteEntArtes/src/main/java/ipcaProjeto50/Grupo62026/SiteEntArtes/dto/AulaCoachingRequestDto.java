package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AulaCoachingRequestDto(
        String professorId,
        String estudioId,
        LocalDate dataAula,
        LocalTime horaInicio,
        LocalTime horaFim,
        Integer maxAlunos,      // opcional — se null, usa o default da BD (8)
        String modalidadeId

) {}