package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "aula_coaching")
public class AulaCoaching {
    @Id
    @Column(name = "aula_id", nullable = false)
    private Integer id;

    @ColumnDefault("8")
    @Column(name = "max_alunos", nullable = false)
    private Integer maxAlunos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado", nullable = false)
    private EstadoAula estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modalidade_id", nullable = false)
    private Modalidade modalidade;


}