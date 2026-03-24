package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "pagamento")
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpagamento", nullable = false)
    private Integer id;

    @Column(name = "valor_pagamento", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPagamento;

    @ColumnDefault("0")
    @Column(name = "pago", nullable = false)
    private Boolean pago;

    @Lob
    @Column(name = "descricao")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idutilizador", nullable = false)
    private Utilizadore idutilizador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_pagamento", nullable = false)
    private TipoPagamento idTipoPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aula_id")
    private Aula aula;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(name = "data_confirmado")
    private LocalDate dataConfirmado;


}