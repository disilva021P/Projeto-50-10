package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

public class HorariosEncarregadoController {
    //TODO: SÓ DEI COPY PASTE DA COODENACAO
    private final AulaService aulaService;
    public HorariosEncarregadoController(AulaService aulaService){
        this.aulaService=aulaService;
    }
    @GetMapping
    public ResponseEntity<List<Aula>> horarioDia(
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        // Se "data" for null, usa a data de hoje
        LocalDate dataBusca = (data != null) ? data : LocalDate.now();

        List<Aula> aulas = aulaService.findByDataAula(dataBusca);

        if (aulas.isEmpty()) {
            return ResponseEntity.noContent().build(); // Opcional: retorna 204 se não houver aulas
        }

        return ResponseEntity.ok(aulas);
    }
}
