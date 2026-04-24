package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Notificacoes;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.NotificacoeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.NotificacoesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Adiciona esta linha para testar
public class NotificacoesController {
    private final NotificacoesService notificacoesService;

    @GetMapping("/me")
    public ResponseEntity<Page<NotificacoeDto>> getMinhasNotificacoes(
            @RequestParam String userId,
            Pageable pageable) {
        return ResponseEntity.ok(notificacoesService.findNotificacoesUtilizador(userId, pageable));
    }

    @PutMapping("/{id}/ler") // Alterado para PutMapping
    public ResponseEntity<Void> marcarComoLida(@PathVariable String id) {
        notificacoesService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }
}