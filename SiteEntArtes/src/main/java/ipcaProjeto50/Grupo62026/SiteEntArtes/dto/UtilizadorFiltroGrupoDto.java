package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.time.LocalDate;

public record UtilizadorFiltroGrupoDto(
        String id,
        String nome,
        LocalDate dataNascimento
) {}