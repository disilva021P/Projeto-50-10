package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ListasController {
    private String getUserId() {
        return (String) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }

    private final UtilizadorService utilizadorService;
    private final ModalidadeService modalidadeService;
    private final ProfessorService professorService;
    private final TurmaService turmaService;
    private final EstudioService estudioService;

    @GetMapping("/utilizador/meus-educandos")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<List<UtilizadoreResumoDto>> educandosEncarregado(){
        return ResponseEntity.ok(utilizadorService.findEducandosdeEducador(getUserId()));
    }
    @GetMapping("/modalidade")
    public ResponseEntity<List<ModalidadeDto>> modalidades(){
        return ResponseEntity.ok(modalidadeService.findAll());
    }
    @GetMapping("/professor")
    public ResponseEntity<List<ProfessoreDto>> professores(){
        return ResponseEntity.ok(professorService.findAll());
    }
    @GetMapping("/turma")
    public ResponseEntity<List<TurmaDto>> turmas(){
        return ResponseEntity.ok(turmaService.findAll());
    }
    @GetMapping("/estudio")
    public ResponseEntity<List<EstudioDto>> estudios(){
        return ResponseEntity.ok(estudioService.findAll());
    }
}
