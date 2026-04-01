package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class TurmaAlunoId implements Serializable {
    private static final long serialVersionUID = -611739379849530994L;
    @Column(name = "turma_id", nullable = false)
    private Integer turmaId;

    @Column(name = "aluno_id", nullable = false)
    private Integer alunoId;


}