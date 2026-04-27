package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cancelamentos")
public class Cancelamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilizador_id", nullable = false)
    private Utilizadore utilizador;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marcardo_por", nullable = false)
    private Utilizadore marcardo_por;


    @Lob
    @Column(name = "motivo", nullable = true,length = 500)
    private String motivo;

    @ColumnDefault("0")
    @Column(name = "justificado", nullable = false)
    private Boolean justificado;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "justificado_em")
    private Instant justificadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        if (this.justificado == null) this.justificado = false;
    }
}