package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "inventario_unidades")
public class InventarioUnidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Lob
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoUnidade estado;

    @ColumnDefault("1")
    @Column(name = "disponivel", nullable = false)
    private Boolean disponivel;

    @Column(name = "localizacao", length = 100)
    private String localizacao;

    @Lob
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}