package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "alunos")
public class Aluno {
    @Id
    @Column(name = "utilizador_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "utilizador_id", nullable = false)
    private Utilizadore utilizadores;

    @Lob
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;


}