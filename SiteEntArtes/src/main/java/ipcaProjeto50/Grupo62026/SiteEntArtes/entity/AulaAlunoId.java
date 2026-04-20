package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class AulaAlunoId implements Serializable {
    private static final long serialVersionUID = -787839291304966037L;
    @Column(name = "aula_id", nullable = false)
    private Integer aulaId;

    @Column(name = "aluno_id", nullable = false)
    private Integer alunoId;


}