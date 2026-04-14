package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

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
        BigDecimal precoAluguer,
        List<MultipartFile> imagens
) {}