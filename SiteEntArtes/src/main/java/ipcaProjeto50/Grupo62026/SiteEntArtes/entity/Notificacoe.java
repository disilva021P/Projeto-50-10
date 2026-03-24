package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notificacoes")
public class Notificacoe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilizador_id", nullable = false)
    private Utilizadore utilizador;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Lob
    @Column(name = "mensagem", nullable = false)
    private String mensagem;

    @ColumnDefault("0")
    @Column(name = "lida", nullable = false)
    private Boolean lida;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;


}