package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Pagamentos;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.PagamentoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.PagamentoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentosController {

    private final PagamentoService pagamentoService;
    private final PagamentoRepository pagamentoRepository;

    public PagamentosController(PagamentoService pagamentoService, PagamentoRepository pagamentoRepository) {
        this.pagamentoService = pagamentoService;
        this.pagamentoRepository = pagamentoRepository;
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
    @GetMapping("/exportar-csv")
    public void exportarCsv(@RequestParam int mes, @RequestParam int ano, HttpServletResponse response) throws IOException {
        // 1. Pede a String ao Service
        String conteudoCsv = pagamentoService.exportarRelatorioMensalTexto(mes, ano);

        // 2. Configura a resposta
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Nome dinâmico: pagamentos_04_2026.csv
        String nomeFicheiro = String.format("pagamentos_%02d_%d.csv", mes, ano);
        response.setHeader("Content-Disposition", "attachment; filename=" + nomeFicheiro);

        // 3. Escreve com suporte a acentos
        response.getWriter().write('\ufeff');
        response.getWriter().write(conteudoCsv);
        response.getWriter().flush();
    }
}