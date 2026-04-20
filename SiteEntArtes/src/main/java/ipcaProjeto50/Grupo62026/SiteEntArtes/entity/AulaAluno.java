package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "aula_alunos")
public class AulaAluno {
    @EmbeddedId
    private AulaAlunoId id;

    @MapsId("aulaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @MapsId("alunoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;
    public AulaAluno(Aula aula, Aluno aluno) {
        this.aula = aula;
        this.aluno = aluno;
        // Importante: Inicializar o ID composto com os IDs das entidades
        this.id = new AulaAlunoId(aula.getId(), aluno.getId());
    }

}