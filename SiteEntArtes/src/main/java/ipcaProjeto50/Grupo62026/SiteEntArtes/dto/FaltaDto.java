package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

/**
 * Record para transferência de dados de falta.
 * @param aulaId ID da aula
 * @param utilizadorId ID do aluno/professor
 */
public record FaltaDto(
        String id,
        String aulaId,
        String utilizadorId,
        Boolean justificado,
        String motivo
) {}