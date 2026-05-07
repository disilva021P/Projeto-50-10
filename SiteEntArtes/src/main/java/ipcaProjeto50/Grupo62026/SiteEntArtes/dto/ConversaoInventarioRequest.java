package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class ConversaoInventarioRequest {
    private Integer unidadeId;
    private String nome;
    private String descricao;
    private String tamanho;
    private String cor;
    private String condicao;
    private Boolean isVenda;
    private Boolean isAluguer;
    private Boolean isDoacao;
    private BigDecimal precoVenda;
    private BigDecimal precoAluguer;
    private MultipartFile[] imagens;
}