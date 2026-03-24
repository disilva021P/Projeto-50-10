package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "auditoria_log")
public class AuditoriaLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilizador", nullable = false)
    private Utilizadore idUtilizador;

    @Lob
    @Column(name = "acao", nullable = false)
    private String acao;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;


}