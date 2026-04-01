package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasAuthority('ENCARREGADO')")
@RestController
@RequestMapping("/api/encarregado/horarios")
public class HorariosEncarregadoController {

    private final AulaService aulaService;

    public HorariosEncarregadoController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    /*
     * Devolve aulas do educando num dia específico
     */
    @GetMapping("/dia")
    public ResponseEntity<List<AulaDto>> horarioDia(
            @AuthenticationPrincipal String userId,
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        LocalDate dataBusca = (data != null) ? data : LocalDate.now();
        List<AulaDto> aulas = aulaService.findByDataAula(dataBusca);

        if (aulas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(aulas);
    }

    /*
     * Página principal — devolve horário semanal do educando
     */
    @GetMapping
    public ResponseEntity<List<AulaDto>> buscarHorarioSemanal(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) Integer offset) {

        if (offset == null) offset = 0;
        if (offset != 0 && offset != 1) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(aulaService.buscarHorarioSemana(userId, offset));
    }

    /*
     * Devolve disponibilidades de professores para marcação de coaching
     */
    @GetMapping("/disponibilidades")
    public ResponseEntity<List<AulaDto>> buscarDisponibilidades(
            @AuthenticationPrincipal String userId,
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        // TODO: implementar busca de disponibilidades por data/professor
        return ResponseEntity.ok().build();
    }

    /*
     * Marca uma aula de coaching pontual
     */
    @PostMapping("/coaching")
    public ResponseEntity<AulaDto> marcarCoaching(
            @AuthenticationPrincipal String userId,
            @RequestBody AulaDto aulaDto) {
        // TODO: verificar disponibilidade professor + estúdio + compatibilidade modalidade
        return ResponseEntity.ok().build();
    }

    /*
     * Marca aulas de coaching recorrentes
     */
    @PostMapping("/coaching/recorrente")
    public ResponseEntity<List<AulaDto>> marcarCoachingRecorrente(
            @AuthenticationPrincipal String userId,
            @RequestBody AulaDto aulaDto) {
        // TODO: criar múltiplas aulas, devolver as criadas e as que tiveram conflito
        return ResponseEntity.ok().build();
    }

    /*
     * Cancela uma aula de coaching
     */
    @DeleteMapping("/coaching/{id}")
    public ResponseEntity<Void> cancelarCoaching(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestParam(required = false) String motivo) {
        // TODO: verificar antecedência e aplicar regra de faturação se cancelamento tardio
        return ResponseEntity.ok().build();
    }

    /*
     * Valida a presença do educando numa aula após confirmação do professor
     */
    @PostMapping("/coaching/{id}/validar")
    public ResponseEntity<Void> validarPresenca(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {
        // TODO: marcar encarregado_confirmou = true, notificar coordenação
        return ResponseEntity.ok().build();
    }

    /*
     * Submete justificação de falta do educando
     */
    @PostMapping("/faltas/{id}/justificar")
    public ResponseEntity<Void> justificarFalta(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestParam String justificacao) {
        // TODO: verificar prazo de justificação (configurável), notificar coordenação
        return ResponseEntity.ok().build();
    }
}