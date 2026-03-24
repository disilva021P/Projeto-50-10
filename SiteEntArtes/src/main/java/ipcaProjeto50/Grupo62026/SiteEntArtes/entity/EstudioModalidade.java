package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estudio_modalidade")
public class EstudioModalidade {
    @EmbeddedId
    private EstudioModalidadeId id;

    @MapsId("estudioId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudio_id", nullable = false)
    private Estudio estudio;

    @MapsId("modalidadeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modalidade_id", nullable = false)
    private Modalidade modalidade;


}