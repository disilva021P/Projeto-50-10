package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Pagamentos;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TipoPagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TipoPagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-pagamento")
@RequiredArgsConstructor
public class TipoPagamentoController {

    private final TipoPagamentoRepository tipoPagamentoRepository;
    private final IdHasher idHasher;

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping
    public ResponseEntity<List<TipoPagamentoDto>> listarTodos() {
        return ResponseEntity.ok(
                tipoPagamentoRepository.findAll().stream()
                        .map(t -> new TipoPagamentoDto(
                                idHasher.encode(t.getId()),
                                t.getTipoPagamento()
                        ))
                        .toList()
        );
    }
}