package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/modalidades") // Ajustado para ser mais semântico
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ModalidadesController {

    private final ModalidadeService modalidadeService;

    // Listar todas - Aberto a quem tiver autenticação básica (ou público, dependendo do SecurityConfig)
    @GetMapping
    public ResponseEntity<List<ModalidadeDto>> getAll() {
        return ResponseEntity.ok(modalidadeService.findAll());
    }

    // Obter uma específica por ID (Hasheado)
    @GetMapping("/{id}")
    public ResponseEntity<ModalidadeDto> getById(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(modalidadeService.findByIdDto(id));
    }

    // --- Rotas de Alternância (Restritas à COORDENACAO) ---

    @PostMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<ModalidadeDto> create(@RequestBody ModalidadeDto dto) {
        return ResponseEntity.ok(modalidadeService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<ModalidadeDto> update(@PathVariable String id, @RequestBody ModalidadeDto dto) throws Exception {
        return ResponseEntity.ok(modalidadeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> delete(@PathVariable String id) throws Exception {
        modalidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}