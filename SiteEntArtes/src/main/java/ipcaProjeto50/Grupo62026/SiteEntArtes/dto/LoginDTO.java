package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(@NotBlank(message = "O email não pode estar vazio")
                       @Email(message = "Formato de email inválido")String email,
                       @NotBlank(message = "A password não pode estar vazia")
                       String password){}
