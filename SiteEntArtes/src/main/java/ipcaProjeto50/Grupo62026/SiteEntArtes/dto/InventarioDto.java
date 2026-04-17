package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventarioDto(
        Integer id,
        String nomeArtigo,
        String descricao,
        String tamanho,
        String cor,
        String condicao,
        Integer estadoId,
        String estadoNome,
        Boolean disponivel,
        String localizacao,
        String notas,
        Instant criadoEm,
        Integer imagemId,
        java.util.List<Integer> imagemIds
) {}