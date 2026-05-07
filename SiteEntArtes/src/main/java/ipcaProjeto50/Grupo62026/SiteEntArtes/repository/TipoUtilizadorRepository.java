package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoUtilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface TipoUtilizadorRepository extends JpaRepository<TipoUtilizador, Integer> {
    Optional<TipoUtilizador> findAllByTipoUtilizador(String tipo);

    Optional<TipoUtilizador> findById(@NonNull Integer id);
}