package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Pagamentos;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentosController {
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