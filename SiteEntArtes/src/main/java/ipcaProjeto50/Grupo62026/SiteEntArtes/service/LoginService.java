package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.LoginDTO;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.UtilizadorLog;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadorLogRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UtilizadorLogRepository logRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;

    public String login(LoginDTO loginDto, String ip) throws Exception {
        // 1. Procurar o utilizador (precisamos do ID para verificar bloqueio de conta)
        if (ipEstaBloqueado(ip)) {
            throw new Exception("O seu IP está bloqueado temporariamente por excesso de tentativas.");
        }
        Optional<Utilizadore> utilizadorOpt = utilizadoreRepository.findByEmail(loginDto.email());

        if(utilizadorOpt.isEmpty()) {
            registarTentativa(null,ip,false);
            throw new Exception("Email ou password incorretos.");
        }

        // 2. Verificar se o IP ou a Conta estão bloqueados antes de qualquer tentativa


        if (estaBloqueado(utilizadorOpt.get().getId())) {
            throw new Exception("Esta conta está bloqueada temporariamente.");
        }

        try {
            // 3. Tentar a Autenticação (Spring Security)
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            if (userDetails == null) throw new Exception("Erro interno");

            // 4. Sucesso: Registar log positivo
            registarTentativa(utilizadorOpt.get(), ip, true);

            return jwtService.generateToken(userDetails);

        } catch (AuthenticationException e) {
            // 5. Falha: Registar log negativo (essencial para o contador de bloqueio)
            registarTentativa(utilizadorOpt.get(), ip, false);
            throw new Exception("Email ou password incorretos.");
        }
    }

    // Métodos auxiliares
    private boolean estaBloqueado(Integer idUtilizador) {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_BLOQUEIO);
        return logRepository.countRecentFailures(idUtilizador, limite) >= MAX_TENTATIVAS;
    }

    private boolean ipEstaBloqueado(String ip) {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_BLOQUEIO);
        return logRepository.countFailuresByIp(ip, limite) >= MAX_TENTATIVAS;
    }

    private void registarTentativa(Utilizadore utilizador, String ip, boolean sucesso) {
        UtilizadorLog log = new UtilizadorLog();
        log.setIdUtilizador(utilizador);
        log.setEnderecoIp(ip);
        log.setSucesso(sucesso ? 1 : 0);
        log.setUltimoLogin(LocalDateTime.now());
        logRepository.save(log);
    }
}