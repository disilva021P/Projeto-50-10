package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasAuthority('ALUNO')")
@RestController
@RequestMapping("/api/aluno/horarios")
public class HorariosAlunoController {
    private final AulaService aulaService;
    public HorariosAlunoController(AulaService aulaService){
        this.aulaService=aulaService;
    }
    @GetMapping
    public ResponseEntity<List<Aula>> horarioDia(@AuthenticationPrincipal String userEmail,
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        // Se "data" for null, usa a data de hoje
        LocalDate dataBusca = (data != null) ? data : LocalDate.now();

        List<Aula> aulas = aulaService.buscarAulaporEmail_Data(dataBusca,userEmail);

        if (aulas.isEmpty()) {
            return ResponseEntity.noContent().build(); // Opcional: retorna 204 se não houver aulas
        }

        return ResponseEntity.ok(aulas);
    }
}
