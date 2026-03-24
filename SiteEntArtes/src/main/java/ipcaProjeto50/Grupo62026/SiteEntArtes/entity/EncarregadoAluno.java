package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "encarregado_aluno")
public class EncarregadoAluno {
    @EmbeddedId
    private EncarregadoAlunoId id;

    @MapsId("encarregadoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encarregado_id", nullable = false)
    private Utilizadore encarregado;

    @MapsId("alunoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;


}