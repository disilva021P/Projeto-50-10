package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
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
    private final IdHasher idHasher;
    public MyUserDetailsService(UtilizadoreRepository repositorio,IdHasher idHasher){
        this.repositorio=repositorio;
        this.idHasher=idHasher;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilizadore utilizadore = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado com o email: " + email));
        return User.builder()
                .username(idHasher.encode(utilizadore.getId()))
                .password(utilizadore.getPalavraPasse())
                .authorities(utilizadore.getTipo().getTipoUtilizador())
                .build();
    }
}
