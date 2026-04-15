package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;

import java.io.Serializable;

/**
 * DTO for {@link Mensagen}
 */
public record MensagemCriarDto(String destinatario  , String conteudo) implements Serializable {
}