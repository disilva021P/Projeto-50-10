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

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;



    // Métodos auxiliares


    public boolean ipEstaBloqueado(String ip) {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_BLOQUEIO);
        return logRepository.countFailuresByIp(ip, limite) >= MAX_TENTATIVAS;
    }

    public void registarTentativa(Utilizadore utilizador, String ip, boolean sucesso) {
        UtilizadorLog log = new UtilizadorLog();
        log.setIdUtilizador(utilizador);
        log.setEnderecoIp(ip);
        log.setSucesso(sucesso ? 1 : 0);
        log.setUltimoLogin(LocalDateTime.now());
        logRepository.save(log);
    }
}