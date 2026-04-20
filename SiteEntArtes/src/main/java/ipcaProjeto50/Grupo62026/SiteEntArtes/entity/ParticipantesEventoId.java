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
public class ParticipantesEventoId implements Serializable {
    private static final long serialVersionUID = 6218274756074153773L;
    @Column(name = "evento_id", nullable = false)
    private Integer eventoId;

    @Column(name = "utilizador_id", nullable = false)
    private Integer utilizadorId;


}