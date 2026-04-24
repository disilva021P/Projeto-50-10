package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventoDto(
        String id,
        String nome,
        String descricao,
        LocalDate dataEvento,
        LocalTime horaInicio,
        LocalTime horaFim,
        String local,
        UtilizadoreResumoDto criadoPor
) implements Serializable {}