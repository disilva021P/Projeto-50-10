package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen}
 */
public record MensagenPreviewDto(String id, String nome, String conteudo,String horas) implements Serializable {

}