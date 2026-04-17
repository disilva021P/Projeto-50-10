package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "estado_aula")
public class EstadoAula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estado_aula", nullable = false)
    private Integer id;

    @Column(name = "estado", nullable = false, length = 45)
    private String estado;

    @Lob
    @Column(name = "descricao",columnDefinition = "TEXT")
    private String descricao;


}