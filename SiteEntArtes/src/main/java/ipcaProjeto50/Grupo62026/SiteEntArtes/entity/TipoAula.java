package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_aula")
public class TipoAula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_aula", nullable = false)
    private Integer id;

    @Column(name = "tipo_aula", nullable = false, length = 45)
    private String tipoAula;

    @Column(name = "descricao")
    private String descricao;


}