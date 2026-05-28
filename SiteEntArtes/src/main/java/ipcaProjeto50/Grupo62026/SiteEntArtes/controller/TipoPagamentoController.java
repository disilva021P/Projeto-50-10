package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TipoPagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.TipoPagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipospagamento")
@RequiredArgsConstructor
public class TipoPagamentoController {

    private final TipoPagamentoService tipoPagamentoService;

    @GetMapping
    public ResponseEntity<List<TipoPagamentoDto>> listarTodos() {
        return ResponseEntity.ok(tipoPagamentoService.listarTodos());
    }
}
