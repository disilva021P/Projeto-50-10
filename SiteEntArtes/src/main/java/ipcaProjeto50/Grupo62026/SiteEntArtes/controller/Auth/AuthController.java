package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Auth;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.LoginDTO;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.LoginInvalidoException;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.LogService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.LoginService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.UtilizadorService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UtilizadorService utilizadorService;
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginData, HttpServletRequest request) {
        String ip=null;

        try {
            ip = request.getRemoteAddr();
            if (loginService.ipEstaBloqueado(ip)) {
                throw new Exception("O ip está bloqueado por excesso de tentativas!");
            }
            Utilizadore user = utilizadorService.findByEmail(loginData.email())
                    .orElseThrow(() -> new LoginInvalidoException("Email ou Password incorretos"));

            // 2. VERIFICAÇÃO DO ATIVO
            if (!user.getAtivo()) { // Assume que o campo é boolean 'ativo'
                 throw new LoginInvalidoException("Esta conta não pode ser acessada! Contacte a coordenacao.");
            }
            // 1. Autenticar as credenciais
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginData.email(), loginData.password())
            );

            // 2. Obter UserDetails e Gerar Token
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            // 3. Buscar os dados do utilizador na BD
            // 4. Criar um mapa com a resposta estruturada para o Frontend
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("nome", user.getNome());

            // Isto permite ao Next.js saber para que página redirecionar
            if (user.getTipo() != null) {
                response.put("tipoId", user.getTipo().getTipoUtilizador());
            }
            loginService.registarTentativa(user,ip, true);
            return ResponseEntity.ok(response);

        } catch (LoginInvalidoException e) {
                loginService.registarTentativa(null,ip, false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }
        catch (AuthenticationException e) {
            loginService.registarTentativa(null,ip, false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou Password incorretos");
        } catch (Exception e) {
            loginService.registarTentativa(null,ip, false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}