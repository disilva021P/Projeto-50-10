package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Modalidade;

import java.io.Serializable;

/**
 * DTO for {@link Modalidade}
 */
public record ModalidadeDto(String id, String nome) implements Serializable {
}