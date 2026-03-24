package ipcaProjeto50.Grupo62026.SiteEntArtes.service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${JWT_SECRET}")
    private String secretKey;

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        // Extraímos as roles do objeto UserDetails e pomos numa "gaveta" própria
        String role = user.getAuthorities().iterator().next().getAuthority();

        claims.put("role", role); // <--- AQUI ESTÁ A TUA CONFIRMAÇÃO EXTRA

        return Jwts.builder()
                .setClaims(claims) // Adiciona as gavetas extra
                .setSubject(user.getUsername()) // O Subject fica só com o Email (Limpo!)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        String role = claims.get("role", String.class); // Busca a string "COORDENACAO"
        return role;
    }
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }



    public boolean isTokenValid(String jwt, String userEmail) {
        return true;//TODO: ADICIONAR LÓGICA
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}