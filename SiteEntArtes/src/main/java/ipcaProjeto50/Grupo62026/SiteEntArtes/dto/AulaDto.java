package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.HashidDeserializer;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.HashidSerializer;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Modalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoAula;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula}
 */
public record AulaDto(@JsonSerialize(using = HashidSerializer.class)
                      @JsonDeserialize(using = HashidDeserializer.class)
        Integer id, Modalidade modalidade, Estudio estudio, Integer maxAlunos, Integer duracaoMinutos,
                      LocalDate dataAula, LocalTime horaInicio, LocalTime horaFim, EstadoAula estado,
                      TipoAula tipoAula) implements Serializable {
}