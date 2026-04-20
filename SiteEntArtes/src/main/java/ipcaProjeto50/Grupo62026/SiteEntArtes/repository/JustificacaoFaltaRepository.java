package ipcaProjeto50.Grupo62026.SiteEntArtes.repository;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.JustificacaoFalta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JustificacaoFaltaRepository extends JpaRepository<JustificacaoFalta, Integer> {

    // Buscar justificação de uma falta específica
    Optional<JustificacaoFalta> findByIdfalta_Id(Integer cancelamentoId);

    // Justificações pendentes (ainda não aceites)
    List<JustificacaoFalta> findByAceite(Boolean aceite);
}