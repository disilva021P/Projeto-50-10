package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditarUtilizadorDto(

        @NotNull
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        @NotNull
        @NotBlank(message = "O email é obrigatório")
        @Email
        String email, // Adicionei o email, caso precises de editar também
        @NotNull
        @NotBlank(message = "O nif é obrigatório")
        String nif,

        @NotNull
        @NotBlank(message = "O telefone é obrigatório")
        String telefone,
        @NotNull
        LocalDate dataNascimento,
        BigDecimal valorHora,
        Boolean professorExterno,
        String notasProfessor
) {
}