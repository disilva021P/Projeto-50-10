package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "descricao", nullable = false,columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tamanho", nullable = false, length = 50)
    private String tamanho;

    @Column(name = "cor")
    private String cor;

    @Column(name = "condicao")
    private String condicao; // 'Novo', 'Como novo', 'Bom estado', 'Usado'

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dono_utilizador_id", nullable = false)
    private Utilizadore donoUtilizador;

    @ColumnDefault("0")
    @Column(name = "tipo_negocio")
    private Integer tipoNegocio; // 0: Doação, 1: Venda, 2: Aluguer

    @OneToMany(mappedBy = "artigo", fetch = FetchType.LAZY)
    private List<InventarioUnidade> unidades = new ArrayList<>();

    @ColumnDefault("0")
    @Column(name = "arquivado", nullable = false)
    private Boolean arquivado;

    @Column(name = "preco", precision = 10, scale = 2)
    private BigDecimal preco;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}

