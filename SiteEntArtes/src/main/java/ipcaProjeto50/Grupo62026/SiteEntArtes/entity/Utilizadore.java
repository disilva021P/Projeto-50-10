package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "utilizadores")
public class Utilizadore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 9)
    private String telefone;

    @Column(name = "palavra_passe", nullable = false)
    private String palavraPasse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo", nullable = false)
    private TipoUtilizador tipo;

    @ColumnDefault("1")
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "editado_em", nullable = false)
    private Instant editadoEm;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Lob
    @Column(name = "nif", nullable = false, columnDefinition = "TEXT")
    private String nif;
    public boolean isAluno(){
        return this.tipo.getTipoUtilizador().equals("ROLE_ALUNO");
    }
    public boolean isProfessor(){
        return this.tipo.getTipoUtilizador().equals("ROLE_PROFESSOR");
    }
    public boolean isCoordenacao(){
        return this.tipo.getTipoUtilizador().equals("ROLE_COORDENACAO");
    }
    public boolean isEncarregado(){
        return this.tipo.getTipoUtilizador().equals("ROLE_Encarregado");
    }

}