package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UtilizadoreRepository repositorio; // O teu repositório de utilizadores
    public MyUserDetailsService(UtilizadoreRepository repositorio){
        this.repositorio=repositorio;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilizadore usuario = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado"));
        System.out.println("ESTOU AQUI____");
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPalavraPasse()) // Deve estar encriptada com BCrypt na BD
                .authorities(usuario.getTipo().getTipoUtilizador().toUpperCase()) // Ou as roles que tiveres
                .build();
    }
}
