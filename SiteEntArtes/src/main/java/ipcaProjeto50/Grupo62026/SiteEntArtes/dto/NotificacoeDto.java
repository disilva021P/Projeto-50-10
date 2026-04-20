package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link Notificacoe}
 */
public record NotificacoeDto(String id, UtilizadoreResumoDto utilizador, String titulo, String mensagem, Boolean lida,
                             Instant criadaEm) implements Serializable {
}