package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

public record InventarioAdicionarRequest(
        String nome,
        String descricao,
        Integer donoUtilizadorId,
        Integer estadoId,
        Boolean disponivel,
        String localizacao,
        String notas
) {}