package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.util.List;

public record GrupoRequestDto(
        String nome,
        java.util.List<String> membrosIds
) {}