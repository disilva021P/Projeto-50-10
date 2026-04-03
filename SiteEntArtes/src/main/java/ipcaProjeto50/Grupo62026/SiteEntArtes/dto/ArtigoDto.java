package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ArtigoDto(
        Integer id,
        String nome,
        String descricao,
        String tamanho,
        String donoNome,
        Integer  paraVenda,    // true = venda, false = doação/aluguer?
        BigDecimal preco,
        Instant criadoEm
) {}