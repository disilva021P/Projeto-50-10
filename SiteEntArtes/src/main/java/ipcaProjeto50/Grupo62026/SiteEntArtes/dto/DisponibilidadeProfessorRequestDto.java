package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadeProfessorRequestDto(String id, String professorid, Integer diaSemana, LocalTime horaInicio,
                                          LocalTime horaFim, LocalDate validoDe,
                                          LocalDate validoAte) implements Serializable {
}
