package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.DisponibilidadeProfessor;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Professore;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link DisponibilidadeProfessor}
 */
public record DisponibilidadeProfessorDto(String id, ProfessoreDto professor, Integer diaSemana, LocalTime horaInicio,
                                          LocalTime horaFim, LocalDate validoDe,
                                          LocalDate validoAte) implements Serializable {
}