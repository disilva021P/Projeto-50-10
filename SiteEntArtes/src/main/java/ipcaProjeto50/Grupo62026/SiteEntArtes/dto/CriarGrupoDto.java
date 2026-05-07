package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.util.List;

public record CriarGrupoDto(
        String nome,
        List<String> membrosHashedIds
) {}
