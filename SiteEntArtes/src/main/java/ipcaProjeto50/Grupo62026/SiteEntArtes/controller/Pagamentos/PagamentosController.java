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

    // GET /api/pagamentos
    // Apenas COORDENACAO pode ver todos os pagamentos
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping
    public ResponseEntity<List<PagamentoDto>> listarTodos() {
        return ResponseEntity.ok(pagamentoService.listarTodos());
    }

    // GET /api/pagamentos/{id}
    // Todos os roles autenticados podem buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDto> buscarPorId(@PathVariable String id) {
        return pagamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/pagamentos
    // Apenas COORDENACAO pode criar pagamentos
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @PostMapping
    public ResponseEntity<PagamentoDto> criar(@RequestBody PagamentoDto dto) {
        try {
            PagamentoDto criado = pagamentoService.criar(dto);
            return ResponseEntity.status(201).body(criado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/pagamentos/{id}
    // Apenas COORDENACAO pode editar pagamentos
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDto> atualizar(
            @PathVariable String id,
            @RequestBody PagamentoDto dto) {
        PagamentoDto atualizado = pagamentoService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    // PATCH /api/pagamentos/{id}/confirmar
    // COORDENACAO confirma pagamentos
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PagamentoDto> confirmar(@PathVariable String id) {
        PagamentoDto confirmado = pagamentoService.confirmar(id);
        return ResponseEntity.ok(confirmado);
    }

    // DELETE /api/pagamentos/{id}
    // Apenas COORDENACAO pode eliminar
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        pagamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/pagamentos/utilizador/{id}?offset=0
    // ALUNO e ENCARREGADO veem os próprios pagamentos; COORDENACAO pode ver qualquer um
    @PreAuthorize("hasAnyRole('COORDENACAO','ALUNO','ENCARREGADO')")
    @GetMapping("/utilizador/{id}")
    public ResponseEntity<List<PagamentoDto>> listarPorUtilizador(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(pagamentoService.listarPorUtilizador(id, offset));
    }

    // GET /api/pagamentos/utilizador/{id}/paginado
    @PreAuthorize("hasAnyRole('COORDENACAO','ALUNO','ENCARREGADO')")
    @GetMapping("/utilizador/{id}/paginado")
    public ResponseEntity<PagedModel<PagamentoDto>> listarPorUtilizadorPaginado(
            @PathVariable String id,
            Pageable pageable) {
        return ResponseEntity.ok(pagamentoService.findAllPorUtilizador(id, pageable));
    }

    // GET /api/pagamentos/utilizador/{id}/estatisticas?offset=0
    // ALUNO e ENCARREGADO veem as próprias estatísticas; COORDENACAO pode ver qualquer uma
    @PreAuthorize("hasAnyRole('COORDENACAO','ALUNO','ENCARREGADO')")
    @GetMapping("/utilizador/{id}/estatisticas")
    public ResponseEntity<AlunoEstatisiticaDto> estatisticasAluno(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(pagamentoService.obterEstatisticasAluno(id, offset));
    }

    // GET /api/pagamentos/estatisticas/coordenacao
    // Apenas COORDENACAO
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping("/estatisticas/coordenacao")
    public ResponseEntity<PagamentosEstatisiticaCoordenacao> estatisticasCoordenacao() {
        return ResponseEntity.ok(pagamentoService.EstatisticasCoordenacao());
    }

    // GET /api/pagamentos/estatisticas/despesas
    // Apenas COORDENACAO
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping("/estatisticas/despesas")
    public ResponseEntity<DespesasEstatisticaDto> estatisticasDespesas() {
        return ResponseEntity.ok(pagamentoService.DespesasEstatistica());
    }

    // GET /api/pagamentos/professor/{id}/estatisticas?offset=0
    // PROFESSOR vê as próprias estatísticas; COORDENACAO pode ver qualquer uma
    @PreAuthorize("hasAnyRole('COORDENACAO','PROFESSOR')")
    @GetMapping("/professor/{id}/estatisticas")
    public ResponseEntity<ProfessorEstatisticaDto> estatisticasProfessor(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(pagamentoService.EstatisticaProfessor(id, offset));
    }

    // GET /api/pagamentos/relatorio?mes=4&ano=2025
    // Apenas COORDENACAO exporta relatórios
    @PreAuthorize("hasAuthority('COORDENACAO')")
    @GetMapping("/relatorio")
    public ResponseEntity<byte[]> exportarRelatorioMensal(
            @RequestParam int mes,
            @RequestParam int ano) {
        String csv = pagamentoService.exportarRelatorioMensalTexto(mes, ano);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "relatorio_" + ano + "_" + mes + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}