package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilizadoreRepository extends JpaRepository<Utilizadore, Integer> {
    Optional<Utilizadore> findByEmail(String email);
}