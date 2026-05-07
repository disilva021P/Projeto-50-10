package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TransacaoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.TransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody TransacaoRequest request) {
        try {
            transacaoService.realizarTransacao(request);
            return ResponseEntity.ok("Transação concluída com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/meus-alugueres")
    public ResponseEntity<?> meusAlugueres(@RequestParam String compradorId) {
        try {
            return ResponseEntity.ok(transacaoService.listarAlugueresAtivos(compradorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable String id) {
        try {
            transacaoService.devolverArtigo(id);
            return ResponseEntity.ok("Artigo devolvido com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
