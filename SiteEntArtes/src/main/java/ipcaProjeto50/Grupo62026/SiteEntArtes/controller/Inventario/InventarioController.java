package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Inventario;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioAdicionarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioEditarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<Page<InventarioDto>> listar(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "12")       int size,
            @RequestParam(defaultValue = "criadoEm") String sortBy,
            @RequestParam(defaultValue = "desc")     String direction,
            @RequestParam(required = false)          String nome,
            @RequestParam(required = false)          Integer estadoId,
            @RequestParam(required = false)          String tamanho,
            @RequestParam(required = false)          String cor,
            @RequestParam(required = false)          String condicao
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                inventarioService.filtrarInventario(nome, estadoId, tamanho, cor, condicao, pageable)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Integer id) {
        inventarioService.removerDoInventario(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioDto> editar(
            @PathVariable Integer id,
            @RequestBody InventarioEditarRequest request
    ) {
        return ResponseEntity.ok(inventarioService.editarUnidade(id, request));
    }

    @PostMapping
    public ResponseEntity<InventarioDto> adicionar(
            @RequestBody InventarioAdicionarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventarioService.adicionarAoInventario(request));
    }
}