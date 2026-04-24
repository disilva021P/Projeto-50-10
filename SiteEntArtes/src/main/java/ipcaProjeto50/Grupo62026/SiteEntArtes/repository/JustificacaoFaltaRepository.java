package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.JustificacaoFalta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JustificacaoFaltaRepository extends JpaRepository<JustificacaoFalta, Integer> {

    // Procura na tabela JustificacaoFalta através do ID da entidade Cancelamento (idfalta)
    Optional<JustificacaoFalta> findByIdfalta_Id(Integer faltaId);
}