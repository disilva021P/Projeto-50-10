package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeriadosService {

    @Autowired
    @Lazy
    private FeriadosService self;
    @Cacheable("feriados")
    public List<LocalDate> diasFeriados() {
        HolidayManager manager = HolidayManager.getInstance(
                ManagerParameters.create(HolidayCalendar.PORTUGAL)
        );        int anoAtual = LocalDate.now().getYear();
        // Feriados nacionais
        Set<Holiday> holidays = manager.getHolidays(anoAtual);
        // Converter para lista mutável
        List<LocalDate> lista = holidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toCollection(ArrayList::new));
        // ➕ Feriado municipal de Braga (São João - 24 de junho)
        lista.add(LocalDate.of(anoAtual, 6, 24));
        // Ordenar e remover duplicados (por segurança)
        return lista.stream()
                .distinct()
                .sorted()
                .toList();
    }
    public boolean isFeriado(LocalDate data) {
        return self.diasFeriados().contains(data);
    }
}
