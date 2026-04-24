package ipcaProjeto50.Grupo62026.SiteEntArtes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class JwtService {

<<<<<<< HEAD
    @Value("${JWT_SECRET}")
    private String secretKey;

    // ─── GERAR TOKEN ─────────────────────────────────────────────
    public String generateToken(UserDetails user) {

        Map<String, Object> claims = new HashMap<>();

        // 👇 remove ROLE_ para manter padrão limpo
        String role = user.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
=======
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService=userDetailsService;
>>>>>>> 92b3ffea007f843f4deef34cab6ce50ae6fad435
    }

    // ─── EXTRAIR ROLE ─────────────────────────────────────────────
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    // ─── USERNAME ────────────────────────────────────────────────
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ─── VALIDAÇÃO ──────────────────────────────────────────────
    public boolean isTokenValid(String jwt, String userId) {
        final String username = extractUsername(jwt);
        return (username.equals(userId)) && !isTokenExpired(jwt);
    }

    // ─── EXPIRAÇÃO ───────────────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ─── KEY ─────────────────────────────────────────────────────
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}