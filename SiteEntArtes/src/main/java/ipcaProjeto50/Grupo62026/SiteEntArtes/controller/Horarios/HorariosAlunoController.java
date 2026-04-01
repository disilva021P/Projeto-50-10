package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ALUNO')")
@RestController
@RequestMapping("/api/aluno/horarios")
public class HorariosAlunoController {
    private final AulaService aulaService;

    /*
    Esta função devolve aulas de um dia específico
     */

    @GetMapping("/dia")
    public ResponseEntity<List<AulaDto>> horarioDia(@AuthenticationPrincipal String userId,
                                                    @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        // Se "data" for null, usa a data de hoje
        LocalDate dataBusca = (data != null) ? data : LocalDate.now();

        List<AulaDto> aulas = aulaService.buscarAulaporEmail_Data(dataBusca,userId);

        if (aulas.isEmpty()) {
            return ResponseEntity.noContent().build(); // Opcional: retorna 204 se não houver aulas
        }

        return ResponseEntity.ok(aulas);
    }
    /*
    Esta função devolve uma aula especifica
     */
    @GetMapping("/{id}")
    public ResponseEntity<AulaDto> horarioAula(@AuthenticationPrincipal String userId,@PathVariable String id) {

        //TODO: Aula especifica, verificar se aluno faz parte da aula!

        return ResponseEntity.ok().build();
    }
    /*
    Pagina principal de horarios, devolve todas as aulas da semana atual ou seguinte
     */
    @GetMapping
    public List<AulaDto> buscarHorarioSemanal(@AuthenticationPrincipal String userId, Integer offset) {
        if (offset == null) offset = 0;
        if (offset!=0 && offset!=1) return null;
        // Agora fazes a query ao banco de dados entre estas duas datas
        return aulaService.buscarHorarioSemana(userId,offset);
    }
    /*
    TODO:Recebe disponibilidade dos professores
     */
    @GetMapping("/disponibilidades")
    public ResponseEntity<List<AulaDto>> buscarDisponibilidades(
            @AuthenticationPrincipal String userId,
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        LocalDate dataBusca = (data != null) ? data : LocalDate.now();
        // TODO: implementar busca de disponibilidades de professores
        return ResponseEntity.ok().build();
    }
    @PostMapping("/coaching")
    public ResponseEntity<AulaDto> marcarCoaching(
            @AuthenticationPrincipal String userId,
            @RequestBody AulaDto aulaDto) {
        // TODO: implementar marcação de aula de coaching
        return ResponseEntity.ok().build();
    }
    @PostMapping("/coaching/{id}/validar")
    public ResponseEntity<Void> validarPresenca(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {
        // TODO: implementar validação de presença
        return ResponseEntity.ok().build();
    }
}
