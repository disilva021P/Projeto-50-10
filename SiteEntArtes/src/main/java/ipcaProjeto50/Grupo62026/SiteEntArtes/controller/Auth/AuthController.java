package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Auth;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.LoginDTO;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoUtilizador;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TipoUtilizadorRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final LoginService loginService;
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginData, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        try {
            String token = loginService.login(loginData,ip);
            return (token == null || token.isBlank()) ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro inesperado") : ResponseEntity.ok().body(token);
        }catch (Exception e) {
        if (e.getMessage().contains("bloqueado")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    }
}
