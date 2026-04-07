package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "imagens_unidade")
public class ImagensUnidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "unidade_id", nullable = false)
    private Integer unidadeId;

    @Lob
    @Column(name = "url_imagem", columnDefinition = "MEDIUMBLOB")
    private byte[] urlImagem;
}