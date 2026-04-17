package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "artigos")
public class Artigo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Lob
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tamanho", nullable = false, length = 50)
    private String tamanho;

    @Column(name = "cor", length = 50)
    private String cor;

    @Column(name = "condicao")
    private String condicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dono_utilizador_id", nullable = false)
    private Utilizadore donoUtilizador;

    @Column(name = "is_venda", nullable = false)
    private Boolean isVenda = false;

    @Column(name = "is_aluguer", nullable = false)
    private Boolean isAluguer = false;

    @Column(name = "is_doacao", nullable = false)
    private Boolean isDoacao = false;

    @ColumnDefault("0")
    @Column(name = "arquivado", nullable = false)
    private Boolean arquivado = false;

    @Column(name = "preco_venda", precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "preco_aluguer", precision = 10, scale = 2)
    private BigDecimal precoAluguer;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @OneToMany(mappedBy = "artigo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventarioUnidade> unidades = new ArrayList<>();
}