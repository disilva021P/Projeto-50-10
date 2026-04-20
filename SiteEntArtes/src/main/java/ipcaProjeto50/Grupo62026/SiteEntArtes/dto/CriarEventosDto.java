package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record CriarEventosDto(
        String nome,
        String descricao,
        LocalDate dataEvento,
        String local,
        List<String> participantesIds  // hashed ids dos utilizadores
) implements Serializable {}