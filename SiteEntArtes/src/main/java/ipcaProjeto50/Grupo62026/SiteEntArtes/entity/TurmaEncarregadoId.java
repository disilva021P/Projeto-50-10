package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class TurmaEncarregadoId implements Serializable {

    @Column(name = "turma_id")
    private Integer turmaId;

    @Column(name = "encarregado_id")
    private Integer encarregadoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TurmaEncarregadoId)) return false;
        TurmaEncarregadoId that = (TurmaEncarregadoId) o;
        return Objects.equals(turmaId, that.turmaId) && Objects.equals(encarregadoId, that.encarregadoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turmaId, encarregadoId);
    }
}