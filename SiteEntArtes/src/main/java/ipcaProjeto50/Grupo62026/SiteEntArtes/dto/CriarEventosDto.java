package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CriarEventosDto(
        String nome,
        String descricao,
        LocalDate dataEvento,
        LocalTime horaInicio,
        LocalTime horaFim,
        String local,
        BigDecimal preco,
        Integer maxParticipantes,
        List<String> participantesIds  // hashed ids dos utilizadores
) implements Serializable {}