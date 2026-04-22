package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventarioDto(
        String id,
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
        String imagemId,
        java.util.List<String> imagemIds
) {}