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
public class EstudioModalidadeId implements Serializable {
    private static final long serialVersionUID = -6411517736459762957L;
    @Column(name = "estudio_id", nullable = false)
    private Integer estudioId;

    @Column(name = "modalidade_id", nullable = false)
    private Integer modalidadeId;


}