package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorariosController {

    @GetMapping
    public ResponseEntity<String> horarios(Authentication authentication) {

        String username = authentication.getName();

        var roles = authentication.getAuthorities();

        boolean isAdmin = roles.stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_COORDENACAO"));

        if (isAdmin) {
            System.out.println("Admin a fazer request");
        } else {
            System.out.println("User normal");
        }

        return ResponseEntity.ok("Ola Mundo!");
    }

}