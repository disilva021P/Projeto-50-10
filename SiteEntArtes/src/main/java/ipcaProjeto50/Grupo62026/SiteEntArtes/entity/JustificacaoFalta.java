package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "justificacao_falta")
public class JustificacaoFalta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    // PDF da justificação submetido pelo encarregado/professor
    @Column(name = "justificacao_pdf", nullable = false)
    private byte[] justificacaoPdf;

    // Falta (cancelamento) a que esta justificação pertence
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idfalta", nullable = false)
    private Cancelamento idfalta;

}

