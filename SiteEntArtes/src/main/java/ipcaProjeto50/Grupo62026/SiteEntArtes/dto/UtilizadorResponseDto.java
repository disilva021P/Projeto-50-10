package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UtilizadorResponseDto(
        String id,
        String nome,
        String email,
        String nif,
        String telefone,
        String tipoUtilizador,
        Boolean ativo,
        LocalDate dataNascimento,
        LocalDateTime criadoEm,
        Double valorHora,
        Boolean professorExterno,
        List<TurmaDto> turmas,
        List<ModalidadeDto> modalidades,
        List<UtilizadoreResumoDto> educandos,
        String encarregadoNome
) {}