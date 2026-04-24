package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Professore;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Professore}
 */
public record   ProfessoreDto(UtilizadoreResumoDto utilizadores, BigDecimal valorHora,
                            Boolean professorExterno) implements Serializable {
}