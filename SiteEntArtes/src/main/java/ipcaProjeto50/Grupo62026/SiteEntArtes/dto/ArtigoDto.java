package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ArtigoDto(
        String id,
        String nome,
        String descricao,
        String tamanho,
        String cor,
        String condicao,
        String donoId,
        String donoNome,
        Boolean isVenda,
        Boolean isAluguer,
        Boolean isDoacao,
        BigDecimal precoVenda,
        BigDecimal precoAluguer,
        Instant criadoEm,
        Integer estadoUnidadeId,
        String estadoUnidadeNome,
        String imagemId,
        List<String> imagemIds
) {}