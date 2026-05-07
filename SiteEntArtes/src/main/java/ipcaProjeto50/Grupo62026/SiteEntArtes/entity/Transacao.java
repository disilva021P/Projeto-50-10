package ipcaProjeto50.Grupo62026.SiteEntArtes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transacao")
@Getter @Setter
@NoArgsConstructor
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artigo_id", nullable = false)
    private Artigo artigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Utilizadore comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Utilizadore vendedor;

    @Column(nullable = false)
    // Se o tipo na BD for ENUM, o JPA mapeia como String sem problemas,
    // mas o nullable=false é obrigatório aqui.
    private String tipo;

    @Column(name = "data_transacao", nullable = false, updatable = false)
    private Instant dataTransacao = Instant.now();

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim_prevista")
    private LocalDate dataFimPrevista;

    @Column(name = "data_devolucao_real")
    private LocalDate dataDevolucaoReal;

    @Column(name = "valor_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "estado_transacao")
    private String estadoTransacao = "CONCLUIDA";
}