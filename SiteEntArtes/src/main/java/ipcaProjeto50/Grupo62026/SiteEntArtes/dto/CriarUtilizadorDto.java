package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;

public record CriarUtilizadorDto(
        String nome,
        String email,
        String nif,
        String telefone,
        String id_tipoUtilizador,
        LocalDate dataNascimento,
        String nif,
        String palavraPasseTemporaria

) {}