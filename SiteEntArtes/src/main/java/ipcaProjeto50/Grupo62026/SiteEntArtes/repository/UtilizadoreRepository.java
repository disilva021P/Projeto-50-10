package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UtilizadoreRepository extends JpaRepository<Utilizadore, Integer> {

    Optional<Utilizadore> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Utilizadore> findAllByTipo_TipoUtilizador(String tipoUtilizador, Pageable pageable);

    List<Utilizadore> findByAtivo(Boolean ativo);

    List<Utilizadore> findByTipo_TipoUtilizadorAndAtivo(String tipoUtilizador, Boolean ativo);


}




















