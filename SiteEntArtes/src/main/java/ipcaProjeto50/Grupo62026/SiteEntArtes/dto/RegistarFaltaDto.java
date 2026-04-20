package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistarFaltaDto(

        // ID da aula onde ocorreu a falta
        @NotNull(message = "O ID da aula é obrigatório")
        String aulaId,

        // ID do utilizador que faltou (aluno ou professor)
        @NotNull(message = "O ID do utilizador é obrigatório")
        String utilizadorId,

        @NotBlank(message = "O motivo é obrigatório")
        String motivo
) {}