package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "configuracoes")
public class Configuracoe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome_config", nullable = false, length = 45)
    private String nomeConfig;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Lob
    @Column(name = "descricao",columnDefinition = "TEXT")
    private String descricao;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "editado_em", nullable = false)
    private Instant editadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "editado_por", nullable = false)
    private Utilizadore editadoPor;


}