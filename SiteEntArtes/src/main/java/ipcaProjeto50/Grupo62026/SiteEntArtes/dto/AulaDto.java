package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula}
 */

public record AulaDto(
        String id, Modalidade modalidade, EstudioDto estudio, Integer duracaoMinutos,
        LocalDate dataAula, LocalTime horaInicio, LocalTime horaFim, EstadoAulaDto estado,
        UtilizadorResumoComTipoDto criadoPor, String notas) implements Serializable {
}