package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Evento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TokenRecuperacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Integer> {
    TokenRecuperacao findByIdUtilizador_Id(Integer idUtilizadorId);

    boolean existsByToken(@Size(max = 127) @NotNull String token);

    void deleteAllByExpiraEmBefore(LocalDateTime now);

    Optional<TokenRecuperacao> findFirstByIdUtilizador_EmailOrderByExpiraEmDesc(String email);
}