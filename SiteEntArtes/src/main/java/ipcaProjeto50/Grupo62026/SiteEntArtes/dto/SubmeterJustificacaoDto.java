package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import jakarta.validation.constraints.NotNull;

public record SubmeterJustificacaoDto(

        // PDF em Base64 enviado pelo frontend
        @NotNull(message = "O ficheiro PDF é obrigatório")
        byte[] justificacaoPdf
) {}
