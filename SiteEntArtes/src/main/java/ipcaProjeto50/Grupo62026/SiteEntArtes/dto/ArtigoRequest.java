package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;

public record ArtigoRequest(
        String nome,
        String descricao,
        String tamanho,
        String cor,
        String condicao,
        Boolean isVenda,
        Boolean isAluguer,
        Boolean isDoacao,
        BigDecimal precoVenda,
        BigDecimal precoAluguer
) {}