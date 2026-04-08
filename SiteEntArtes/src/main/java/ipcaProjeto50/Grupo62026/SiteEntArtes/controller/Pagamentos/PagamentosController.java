package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Pagamentos;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.PagamentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentosController {

    private final PagamentoService pagamentoService;

    public PagamentosController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    // LISTAR TODOS
    @GetMapping
    public List<PagamentoDto> listarTodos() {
        return pagamentoService.listarTodos();
    }

    // BUSCAR POR ID (O ID aqui é String por causa do IdHasher)
    @GetMapping("/{id}")
    public PagamentoDto buscarPorId(@PathVariable String id) {
        return pagamentoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    // CRIAR (Adicionado @RequestBody para o Java ler o JSON que vem do JS)
    @PostMapping
    public PagamentoDto criar(@RequestBody PagamentoDto pagamentoDto) {
        return pagamentoService.criar(pagamentoDto);
    }

    // CONFIRMAR (Mudei para String para bater certo com o Service)
    @PatchMapping("/{id}/confirmar")
    public PagamentoDto confirmar(@PathVariable String id) {
        return pagamentoService.confirmar(id);
    }

    // DELETE (Mudei para String para bater certo com o Service)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        pagamentoService.eliminar(id);
    }
}