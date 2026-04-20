package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Instant;

public record EventoDto(
        String id,
        String nome,
        String descricao,
        LocalDate dataEvento,
        String local,
        UtilizadoreResumoDto criadoPor,
        Instant criadoEm
) implements Serializable {}
