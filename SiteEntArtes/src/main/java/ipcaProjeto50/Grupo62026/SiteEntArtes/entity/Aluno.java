package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "alunos")
@PrimaryKeyJoinColumn(name = "utilizador_id")
public class Aluno extends Utilizadore {
    @Lob
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
}