package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TransacaoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.TransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestBody TransacaoRequest request) {
        transacaoService.realizarTransacao(request);
        return ResponseEntity.ok("Transação concluída com sucesso!");
    }
}
