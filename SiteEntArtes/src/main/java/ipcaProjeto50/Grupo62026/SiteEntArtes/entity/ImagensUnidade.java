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

    @Column(name = "artigo_id", nullable = false)
    private Integer artigoId;

    @Lob
    @Column(name = "url_imagem", columnDefinition = "MEDIUMBLOB")
    private byte[] urlImagem;
}