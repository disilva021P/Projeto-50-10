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
public class EncarregadoAlunoId implements Serializable {
    private static final long serialVersionUID = 3826637893157635510L;
    @Column(name = "encarregado_id", nullable = false)
    private Integer encarregadoId;

    @Column(name = "aluno_id", nullable = false)
    private Integer alunoId;


}