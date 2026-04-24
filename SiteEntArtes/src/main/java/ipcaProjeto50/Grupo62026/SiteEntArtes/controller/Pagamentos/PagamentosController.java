package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Pagamentos;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentosController {

    private final PagamentoService pagamentoService;

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping
    public ResponseEntity<List<PagamentoDto>> listarTodos() {
        try {
            return ResponseEntity.ok(pagamentoService.listarTodos());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDto> buscarPorId(@PathVariable String id) {
        try {
            return pagamentoService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @PostMapping
    public ResponseEntity<PagamentoDto> criar(@RequestBody PagamentoDto dto) {
        try {
            PagamentoDto criado = pagamentoService.criar(dto);
            return ResponseEntity.status(201).body(criado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDto> atualizar(
            @PathVariable String id,
            @RequestBody PagamentoDto dto) {
        try {
            PagamentoDto atualizado = pagamentoService.atualizar(id, dto);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PagamentoDto> confirmar(@PathVariable String id) {
        try {
            PagamentoDto confirmado = pagamentoService.confirmar(id);
            return ResponseEntity.ok(confirmado);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        try {
            pagamentoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAnyRole('COORDENACAO','ALUNO','ENCARREGADO')")
    @GetMapping("/utilizador/{id}")
    public ResponseEntity<List<PagamentoDto>> listarPorUtilizador(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            return ResponseEntity.ok(pagamentoService.listarPorUtilizador(id, offset));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAnyRole('COORDENACAO','ALUNO','ENCARREGADO')")
    @GetMapping("/utilizador/{id}/paginado")
    public ResponseEntity<PagedModel<PagamentoDto>> listarPorUtilizadorPaginado(
            @PathVariable String id,
            Pageable pageable) {
        try {
            return ResponseEntity.ok(pagamentoService.findAllPorUtilizador(id, pageable));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAnyRole('COORDENACAO','ALUNO','ENCARREGADO')")
    @GetMapping("/utilizador/{id}/estatisticas")
    public ResponseEntity<AlunoEstatisiticaDto> estatisticasAluno(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            return ResponseEntity.ok(pagamentoService.obterEstatisticasAluno(id, offset));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping("/estatisticas/coordenacao")
    public ResponseEntity<PagamentosEstatisiticaCoordenacao> estatisticasCoordenacao() {
        try {
            return ResponseEntity.ok(pagamentoService.EstatisticasCoordenacao());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping("/estatisticas/despesas")
    public ResponseEntity<DespesasEstatisticaDto> estatisticasDespesas() {
        try {
            return ResponseEntity.ok(pagamentoService.DespesasEstatistica());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAnyRole('COORDENACAO','PROFESSOR')")
    @GetMapping("/professor/{id}/estatisticas")
    public ResponseEntity<ProfessorEstatisticaDto> estatisticasProfessor(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            return ResponseEntity.ok(pagamentoService.EstatisticaProfessor(id, offset));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping("/relatorio")
    public ResponseEntity<byte[]> exportarRelatorioMensal(
            @RequestParam int mes,
            @RequestParam int ano) {
        try {
            String csv = pagamentoService.exportarRelatorioMensalTexto(mes, ano);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "relatorio_" + ano + "_" + mes + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}