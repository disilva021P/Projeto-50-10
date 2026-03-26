package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasAuthority('COORDENACAO')")
@RestController
@RequestMapping("/api/coordenacao/horarios")
public class HorariosCoordenacaoController {
    private final AulaService aulaService;
    public HorariosCoordenacaoController(AulaService aulaService){
        this.aulaService=aulaService;
    }

    /*@GetMapping
    public ResponseEntity<PagedModel<Aula>> horarios(Authentication authentication, @PageableDefault(size = 10, sort = "dataAula") Pageable paginacao) {
        PagedModel<Aula> pagina = aulaService.findAll(paginacao);
        return ResponseEntity.ok(pagina);
    }*/
    @GetMapping
    public ResponseEntity<List<Aula>> horarioDia(
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate dataBusca = (data != null) ? data : LocalDate.now();

        List<Aula> aulas = aulaService.findByDataAula(dataBusca);

        if (aulas.isEmpty()) {
            return ResponseEntity.noContent().build(); // Opcional: retorna 204 se não houver aulas
        }

        return ResponseEntity.ok(aulas);
    }

}