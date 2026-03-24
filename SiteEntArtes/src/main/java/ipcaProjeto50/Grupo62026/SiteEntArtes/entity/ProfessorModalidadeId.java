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
public class ProfessorModalidadeId implements Serializable {
    private static final long serialVersionUID = -8611283899208807121L;
    @Column(name = "professor_id", nullable = false)
    private Integer professorId;

    @Column(name = "modalidade_id", nullable = false)
    private Integer modalidadeId;


}