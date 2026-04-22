package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

public record InventarioEditarRequest(
        String nome,
        String descricao,
        Boolean disponivel,
        String localizacao,
        String notas
) {}