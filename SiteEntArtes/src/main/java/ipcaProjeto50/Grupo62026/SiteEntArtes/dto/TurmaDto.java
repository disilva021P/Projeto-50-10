package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Turma}
 */
public record TurmaDto(String id, String nome, BigDecimal mensalidade,
                       ModalidadeDto modalidade) implements Serializable {
}