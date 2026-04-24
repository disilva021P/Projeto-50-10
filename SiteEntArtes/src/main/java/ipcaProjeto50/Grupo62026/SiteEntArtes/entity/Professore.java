package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "professores") // Esta tabela conterá apenas os campos específicos
@PrimaryKeyJoinColumn(name = "utilizador_id") // Liga o ID do Professor ao ID do Utilizador
public class Professore extends Utilizadore {



    @ColumnDefault("36.00")
    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @ColumnDefault("0")
    @Column(name = "professor_externo")
    private Boolean professorExterno;

    @Lob
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
}