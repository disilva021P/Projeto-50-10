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
public class AulaProfessoreId implements Serializable {
    private static final long serialVersionUID = 8887274065148871569L;
    @Column(name = "aula_id", nullable = false)
    private Integer aulaId;

    @Column(name = "professor_id", nullable = false)
    private Integer professorId;


}