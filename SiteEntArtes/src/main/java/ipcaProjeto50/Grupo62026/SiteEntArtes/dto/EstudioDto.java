package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;

import java.io.Serializable;

/**
 * DTO for {@link Estudio}
 */
public record EstudioDto(String id, String nome, Integer capacidade) implements Serializable {
}