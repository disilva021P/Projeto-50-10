package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "validacao_aula")
public class ValidacaoAula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @Column(name = "professor_confirmou")
    private Instant professorConfirmou;

    @Column(name = "encarregado_confirmou")
    private Instant encarregadoConfirmou;

    @Column(name = "coordenador_confirmou")
    private Instant coordenadorConfirmou;

    @ColumnDefault("0")
    @Column(name = "validacao_automatica", nullable = false)
    private Boolean validacaoAutomatica;


}