package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioTurmaRequestDto(String id, String idcriadoPor, String idturma, LocalDate dataInicio, LocalDate dataValidade,
                                     Integer diaSemana, Integer duracaoMinutos, LocalTime horaInicio,
                                     LocalTime horaFim, String estudioId) implements Serializable {

}
