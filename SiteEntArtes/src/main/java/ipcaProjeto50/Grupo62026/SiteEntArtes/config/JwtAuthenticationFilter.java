package ipcaProjeto50.Grupo62026.SiteEntArtes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extrair o Header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Verificar se o header existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrair o token (removendo a palavra "Bearer ")
        String jwt = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(jwt); // Assume que tens este método no JwtService

        // 4. Se temos o email e o utilizador ainda não está autenticado no contexto do Spring
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Aqui validamos o token (ajusta conforme o teu JwtService)
            if (jwtService.isTokenValid(jwt, userEmail)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        Collections.emptyList() // Aqui poderias colocar as ROLES/Authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Informar o Spring que o utilizador é válido para este pedido
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}