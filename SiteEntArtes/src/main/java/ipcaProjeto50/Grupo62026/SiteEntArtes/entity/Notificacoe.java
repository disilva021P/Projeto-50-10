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
@Table(name = "notificacoes")
public class Notificacoe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Utilizadore destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id")
    private Utilizadore remetente;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Lob
    @Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "referencia_id", length = 100)
    private String referenciaId;

    @ColumnDefault("0")
    @Column(name = "lida", nullable = false)
    private Boolean lida = false;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm = Instant.now();
}