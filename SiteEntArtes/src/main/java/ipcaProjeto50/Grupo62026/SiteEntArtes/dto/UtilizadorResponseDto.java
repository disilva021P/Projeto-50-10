package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UtilizadorResponseDto(
        String id,
        String nome,
        String email,
        String telefone,
        String tipoUtilizador,
        Boolean ativo,
        LocalDate dataNascimento,
        LocalDateTime criadoEm
) {}