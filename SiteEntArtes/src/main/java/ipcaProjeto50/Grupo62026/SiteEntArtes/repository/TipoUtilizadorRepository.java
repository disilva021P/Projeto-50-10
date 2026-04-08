package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoUtilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoUtilizadorRepository extends JpaRepository<TipoUtilizador, Integer> {

    Optional<TipoUtilizador> findByTipoUtilizador(String tipoUtilizador);
}