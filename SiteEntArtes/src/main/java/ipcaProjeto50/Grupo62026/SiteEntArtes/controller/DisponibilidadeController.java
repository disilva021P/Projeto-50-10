package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.service.DisponibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disponibilidade")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    @GetMapping("/professor/{id}")
    public ResponseEntity<?> getByProfessor(@PathVariable String id) {
        try {
            return ResponseEntity.ok(
                    disponibilidadeService.disponibilidadesByProfessorId(id)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
