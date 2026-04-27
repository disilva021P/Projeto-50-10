package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Auth;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.LoginDTO;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(allowedHeaders = "*")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UtilizadoreRepository utilizadoreRepository;

    @Autowired
    public AuthController(JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          UtilizadoreRepository utilizadoreRepository) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.utilizadoreRepository = utilizadoreRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginData) {
        try {
            // 1. Autenticar as credenciais
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginData.email(), loginData.password())
            );

            // 2. Obter UserDetails e Gerar Token
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            // 3. Buscar os dados do utilizador na BD
            Utilizadore user = utilizadoreRepository.findByEmail(loginData.email())
                    .orElseThrow(() -> new RuntimeException("Utilizador não encontrado após login"));

            // 4. Criar um mapa com a resposta estruturada para o Frontend
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("nome", user.getNome());

            // Isto permite ao Next.js saber para que página redirecionar
            if (user.getTipo() != null) {
                response.put("tipoId", user.getTipo().getTipoUtilizador());
            }

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou Password incorretos");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno no servidor");
        }
    }
}