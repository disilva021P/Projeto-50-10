package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;

public record ParticipanteDto(
        String utilizadorNome,
        String utilizadorEmail,
        boolean pago,
        boolean cancelado
) implements Serializable {
}
