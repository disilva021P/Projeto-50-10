package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "turma_encarregados")
public class TurmaEncarregado {

    @EmbeddedId
    private TurmaEncarregadoId id;

    @MapsId("turmaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @MapsId("encarregadoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encarregado_id", nullable = false)
    private Utilizadore encarregado;

    @Column(name = "inscrito_em", nullable = false)
    private LocalDate inscritoEm;
}