package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_pagamento")
public class TipoPagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtipo_pagamento", nullable = false)
    private Integer id;

    @Column(name = "tipo_pagamento", nullable = false, length = 45)
    private String tipoPagamento;

    @Lob
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;


}