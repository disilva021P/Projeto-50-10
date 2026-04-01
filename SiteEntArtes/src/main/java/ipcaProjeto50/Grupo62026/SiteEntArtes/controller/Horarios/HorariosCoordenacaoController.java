package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import org.springframework.format.annotation.DateTimeFormat;
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

    public HorariosCoordenacaoController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    /*
     * Vista completa de todas as aulas num dia específico
     */
    @GetMapping
    public ResponseEntity<List<AulaDto>> horarioDia(
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
     * Vista semanal completa de todas as aulas da escola
     */
    @GetMapping("/semana")
    public ResponseEntity<List<AulaDto>> horarioSemanal(
            @RequestParam(required = false) Integer offset) {

        if (offset == null) offset = 0;
        if (offset != 0 && offset != 1) return ResponseEntity.badRequest().build();

        // TODO: implementar busca semanal sem filtro por utilizador
        return ResponseEntity.ok().build();
    }

    /*
     * Cria um horário semanal regular para o ano (aulas recorrentes)
     */
    @PostMapping("/regular")
    public ResponseEntity<Aula> criarHorarioRegular(
            @RequestBody AulaDto aulaDto) {
        // TODO: verificar conflitos, gerar todas as aulas futuras, notificar professor
        return ResponseEntity.ok().build();
    }

    /*
     * Altera um horário anual regular e propaga para aulas futuras
     */
    @PutMapping("/regular/{id}")
    public ResponseEntity<Aula> alterarHorarioRegular(
            @PathVariable String id,
            @RequestBody AulaDto aulaDto) {
        // TODO: atualizar registo e recalcular aulas futuras, notificar afetados
        return ResponseEntity.ok().build();
    }

    /*
     * Suspende uma aula regular numa semana específica (ex: feriado)
     */
    @DeleteMapping("/regular/{id}/suspender")
    public ResponseEntity<Void> suspenderAula(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam String motivo) {
        // TODO: cancelar apenas esta ocorrência, manter horário base, notificar intervenientes
        return ResponseEntity.ok().build();
    }

    /*
     * Cria uma aula pontual avulso
     */
    @PostMapping
    public ResponseEntity<Aula> criarAula(
            @RequestBody AulaDto aulaDto) {
        // TODO: verificar conflitos professor + estúdio + modalidade
        return ResponseEntity.ok().build();
    }

    /*
     * Valida definitivamente uma aula (após confirmações de professor e encarregado)
     */
    @PostMapping("/{id}/validar")
    public ResponseEntity<Void> validarAula(
            @PathVariable String id) {
        // TODO: marcar coordenador_confirmou = true, gerar registo financeiro, marcar como "realizada"
        return ResponseEntity.ok().build();
    }

    /*
     * Lista de aulas pendentes de validação pela coordenação
     */
    @GetMapping("/pendentes")
    public ResponseEntity<List<Aula>> aulasPendentes() {
        // TODO: devolver aulas onde professor e encarregado confirmaram mas coordenação ainda não
        return ResponseEntity.ok().build();
    }

    /*
     * Rejeita a validação de uma aula com justificação
     */
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<Void> rejeitarAula(
            @PathVariable String id,
            @RequestParam String motivo) {
        // TODO: notificar professor e encarregado, não gerar registo financeiro
        return ResponseEntity.ok().build();
    }
}