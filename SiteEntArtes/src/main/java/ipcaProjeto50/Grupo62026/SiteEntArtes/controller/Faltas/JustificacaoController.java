package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas;

import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/justificacoes")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JustificacaoController {

    private final JustificacaoService justificacaoService;

    // 1. SUBMETER: Encarregado envia o motivo e o PDF
    // Usamos @RequestParam para o ficheiro e para o texto
    @PostMapping(value = "/{faltaId}/submeter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submeter(
            @PathVariable String faltaId,
            @RequestParam("ficheiro") MultipartFile ficheiro,
            @RequestParam("motivo") String motivo) throws IOException {

        justificacaoService.submeterJustificacao(faltaId, ficheiro.getBytes(), motivo);
        return ResponseEntity.ok("Justificação submetida com sucesso!");
    }

    // 2. VISUALIZAR: Coordenação clica para ver o PDF no browser
    @GetMapping(value = "/{faltaId}/ver-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> verPdf(@PathVariable String faltaId) {
        byte[] pdf = justificacaoService.verConteudoPdf(faltaId);
        return ResponseEntity.ok().body(pdf);
    }
}