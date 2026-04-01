package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "turmas")
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100, columnDefinition = "TEXT")
    private String nome;

    @Column(name = "mensalidade", nullable = false, precision = 10, scale = 2)
    private BigDecimal mensalidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modalidade_id", nullable = false)
    private Modalidade modalidade;

    @ColumnDefault("1")
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;


}