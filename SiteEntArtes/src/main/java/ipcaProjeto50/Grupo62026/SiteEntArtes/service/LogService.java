package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AuditoriaLogDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AuditoriaLog;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AuditoriaLogRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final AuditoriaLogRepository auditoriaRepository;
    private final IdHasher idHasher;
    private final UtilizadoreRepository utilizadoreRepository; // Para associar o utilizador

    // --- LISTAR ---
    public List<AuditoriaLogDto> listarTodos() {
        return auditoriaRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    public Page<AuditoriaLogDto> listarTodos(Pageable pageable) {
        // Buscamos a página de entidades e mapeamos cada uma para DTO
        return auditoriaRepository.findAll(pageable).map(this::convertToDto);
    }
    // --- INSERIR ---
    public void registrarAcao(String idUserHashed, String acao) {
        AuditoriaLog log = new AuditoriaLog();

        // Decodificar o ID do utilizador e procurar a entidade
        Integer userId = idHasher.decode(idUserHashed);
        Utilizadore user = utilizadoreRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        log.setIdUtilizador(user);
        log.setAcao(acao);
        log.setCriadoEm(LocalDateTime.now()); // Define a data/hora atual no momento do log

        auditoriaRepository.save(log);
    }

    // --- CONVERSOR ---
    private AuditoriaLogDto convertToDto(AuditoriaLog log) {
        return new AuditoriaLogDto(
                idHasher.encode(log.getId()),
                new UtilizadoreResumoDto(
                        idHasher.encode(log.getIdUtilizador().getId()),
                        log.getIdUtilizador().getNome()
                ),
                log.getAcao(),
                log.getCriadoEm()
        );
    }
}
