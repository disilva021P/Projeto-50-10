package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public record AulaRequestDto(
        String id, String estudioid, Integer duracaoMinutos,
        LocalDate dataAula, LocalTime horaInicio, LocalTime horaFim,
        String criadoPo, String idHorario, String estado) implements Serializable {
}
