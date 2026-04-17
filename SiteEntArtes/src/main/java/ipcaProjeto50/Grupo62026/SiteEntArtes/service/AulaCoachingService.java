package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaCoaching;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaCoachingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AulaCoachingService {

    private final AulaCoachingRepository aulaCoachingRepository;
    private final IdHasher idHasher;
    private final AulaService aulaService;
    private final EstadoAuloService estadoAuloService;
    private final ModalidadeService modalidadeService;

    // =========================================================================
    // IDs dos estados — ajustar conforme os valores na base de dados
    // =========================================================================
    private static final int ID_ESTADO_PENDENTE   = 1;
    private static final int ID_ESTADO_CONFIRMADA = 2;
    private static final int ID_ESTADO_REALIZADA  = 3;
    private static final int ID_ESTADO_CANCELADA  = 4;

    // =========================================================================
    // Leitura
    // =========================================================================

    public AulaCoachingDto findById(Integer id) throws Exception {
        return convertToAulaCoachingDto(
                aulaCoachingRepository.findById(id)
                        .orElseThrow(() -> new Exception("Aula de coaching não encontrada"))
        );
    }

    public AulaCoachingDto findById(String id) throws Exception {
        return convertToAulaCoachingDto(
                aulaCoachingRepository.findById(idHasher.decode(id))
                        .orElseThrow(() -> new Exception("Aula de coaching não encontrada"))
        );
    }

    public List<AulaCoachingDto> findAllbyAlunoId(String alunoId) {
        return aulaCoachingRepository.buscarAulaCoachingPorAluno(idHasher.decode(alunoId))
                .stream()
                .map(aula -> {
                    try {
                        return convertToAulaCoachingDto(aula);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed", e);
                    }
                })
                .toList();
    }

    public Page<AulaCoachingDto> findAllbyAlunoIdPage(String alunoId, Pageable pageable) {
        Integer idDecoded = idHasher.decode(alunoId);
        Page<AulaCoaching> entityPage = aulaCoachingRepository.buscarAulaCoachingPorAluno(idDecoded, pageable);
        return entityPage.map(aula -> {
            try {
                return convertToAulaCoachingDto(aula);
            } catch (Exception e) {
                throw new RuntimeException("Mapping failed", e);
            }
        });
    }

    /**
     * Devolve todos os coachings (para a coordenação).
     */
    public Page<AulaCoachingDto> findAll(Pageable pageable) {
        return aulaCoachingRepository.findAll(pageable).map(aula -> {
            try {
                return convertToAulaCoachingDto(aula);
            } catch (Exception e) {
                throw new RuntimeException("Mapping failed", e);
            }
        });
    }

    /**
     * Devolve coachings PENDENTES associados a um professor.
     * O professor é identificado pelo ID do utilizador que criou a aula.
     */
    public Page<AulaCoachingDto> findPendentesByProfessorId(String professorId, Pageable pageable) {
        Integer idDecoded = idHasher.decode(professorId);
        Page<AulaCoaching> entityPage = aulaCoachingRepository
                .buscarAulaCoachingPendentesPorProfessor(idDecoded, pageable);
        return entityPage.map(aula -> {
            try {
                return convertToAulaCoachingDto(aula);
            } catch (Exception e) {
                throw new RuntimeException("Mapping failed", e);
            }
        });
    }

    // =========================================================================
    // Escrita
    // =========================================================================

    public AulaCoachingDto salvar(AulaCoachingDto aulaCoachingDto) throws Exception {
        return convertToAulaCoachingDto(aulaCoachingRepository.save(dtoParacoaching(aulaCoachingDto)));
    }

    /**
     * Confirma um coaching — muda o estado de PENDENTE para CONFIRMADA.
     * Lança exceção se o coaching já não estiver no estado PENDENTE.
     */
    @Transactional
    public AulaCoachingDto confirmar(String aulaId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        int estadoAtual = coaching.getEstado().getId();
        if (estadoAtual != ID_ESTADO_PENDENTE) {
            throw new Exception("Só é possível confirmar coachings no estado PENDENTE. Estado atual: "
                    + coaching.getEstado().getEstado());
        }

        EstadoAula estadoConfirmada = estadoAuloService.findbyId(ID_ESTADO_CONFIRMADA);
        coaching.setEstado(estadoConfirmada);
        return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
    }

    /**
     * Valida a realização de um coaching — muda o estado para REALIZADA.
     * Pode ser chamado pelo professor ou pela coordenação.
     * Lança exceção se o coaching já estiver cancelado ou já realizado.
     */
    @Transactional
    public AulaCoachingDto validarRealizacao(String aulaId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        int estadoAtual = coaching.getEstado().getId();
        if (estadoAtual == ID_ESTADO_CANCELADA) {
            throw new Exception("Não é possível validar um coaching cancelado.");
        }
        if (estadoAtual == ID_ESTADO_REALIZADA) {
            throw new Exception("O coaching já foi marcado como realizado.");
        }

        EstadoAula estadoRealizada = estadoAuloService.findbyId(ID_ESTADO_REALIZADA);
        coaching.setEstado(estadoRealizada);
        return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
    }

    /**
     * Valida a presença de um aluno num coaching.
     * Confirma que o aluno está inscrito na aula e regista a sua presença,
     * atualizando o estado para CONFIRMADA se ainda estava PENDENTE.
     */
    @Transactional
    public AulaCoachingDto validarPresenca(String alunoId, String aulaId) throws Exception {
        // Verifica que o aluno pertence a esta aula
        aulaService.buscarAulaAluno(alunoId, aulaId);

        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        // Só avança o estado se ainda estiver pendente
        if (coaching.getEstado().getId() == ID_ESTADO_PENDENTE) {
            EstadoAula estadoConfirmada = estadoAuloService.findbyId(ID_ESTADO_CONFIRMADA);
            coaching.setEstado(estadoConfirmada);
            return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
        }

        return convertToAulaCoachingDto(coaching);
    }

    // =========================================================================
    // Conversão
    // =========================================================================

    public AulaCoachingDto convertToAulaCoachingDto(AulaCoaching aulaCoaching) throws Exception {
        AulaDto aulaPrincipal = aulaService.bucarPorIdDto(aulaCoaching.getId());
        return new AulaCoachingDto(
                aulaPrincipal,
                aulaCoaching.getMaxAlunos(),
                estadoAuloService.converterParaDto(aulaCoaching.getEstado()),
                modalidadeService.converterParaDto(aulaCoaching.getModalidade())
        );
    }

    public AulaCoaching dtoParacoaching(AulaCoachingDto aulaCoaching) throws Exception {
        AulaCoaching coaching = new AulaCoaching();
        coaching.setId(idHasher.decode(aulaCoaching.aulaDto().id()));
        coaching.setMaxAlunos(aulaCoaching.max_alunos());
        coaching.setModalidade(modalidadeService.findbyId(idHasher.decode(aulaCoaching.modalidadeDto().id())));
        return coaching;
    }
}