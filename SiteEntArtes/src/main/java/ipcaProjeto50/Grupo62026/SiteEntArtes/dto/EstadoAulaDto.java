package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;

import java.io.Serializable;

/**
 * DTO for {@link EstadoAula}
 */
public record EstadoAulaDto(String id, String estado) implements Serializable {
}