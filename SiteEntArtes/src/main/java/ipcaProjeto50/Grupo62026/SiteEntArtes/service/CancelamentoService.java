package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancelamentoService {
    private final CancelamentoRepository cancelamentoRepository;
    private final AulaRepository aulaRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;
    private final NotificacoesService notificacoesService;
    private final AulaAlunoRepository aulaAlunoRepository;
    private final AulaProfessoreRepository aulaProfessoreRepository;

    public FaltaDto marcarFalta(FaltaDto faltaDto, String idMarca_por) throws Exception {
        // 1. Descodifica para trabalhar internamente
        Integer idAulaReal = idHasher.decode(faltaDto.aulaId());
        Integer idUserReal = idHasher.decode(faltaDto.utilizadorId());

        Aula aula = aulaRepository.findById(idAulaReal)
                .orElseThrow(() -> new Exception("Aula não encontrada"));

        Utilizadore utilizador = utilizadoreRepository.findById(idUserReal)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));
        Utilizadore marcado_por = utilizadoreRepository.findById(idHasher.decode(idMarca_por))
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        // =========================================================================
        // VALIDAÇÃO INTELIGENTE: ACEITA ALUNOS (DIRETOS OU TURMA) E PROFESSORES
        // =========================================================================
        boolean temVinculoComAAula = false;

        // Se o utilizador a quem estamos a marcar falta for uma Professora
        if (utilizador.isProfessor()) {

            // Verifica na tabela intermédia se esta aula pertence a este professor.
            // O Spring Data JPA resolve isto nativamente se usares a convenção de nomes padrão:
            temVinculoComAAula = aulaProfessoreRepository.existsByAula_IdAndProfessor_Id(idAulaReal, idUserReal);

        } else {
            // Se for um Aluno, fazemos a verificação na tabela AulaAluno
            boolean inscritoDireto = aulaAlunoRepository.existsByAula_IdAndAluno_Id(idAulaReal, idUserReal);

            // Também verificamos se a aula tem um horário/turma associado.
            // Se tiver, e a coordenação o listou na View, consideramos o aluno elegível.
            boolean inscritoNaTurma = false;
            if (aula.getIdHorario() != null && aula.getIdHorario().getIdturma() != null) {
                inscritoNaTurma = true;
            }

            temVinculoComAAula = inscritoDireto || inscritoNaTurma;
        }

        if (!temVinculoComAAula) {
            throw new Exception("O utilizador selecionado não leciona nem está inscrito nesta aula!");
        }
        // =========================================================================

        // 2. Guarda na DB (O teu código original intacto, sem remover nada)
        Cancelamento cancelamento = new Cancelamento();
        cancelamento.setAula(aula);
        cancelamento.setUtilizador(utilizador);
        cancelamento.setMarcardo_por(marcado_por);
        cancelamento.setJustificado(false);
        cancelamento.setMotivo(faltaDto.motivo());
        Cancelamento salvo = cancelamentoRepository.save(cancelamento);

        notificacoesService.criarNotificacao(
                utilizador.getId(),
                marcado_por.getId(),
                "Recebeu uma falta! ",
                "Recebeu falta na aula " + aula.getDataAula() +
                        " (" + aula.getHoraInicio() + " - " + aula.getHoraFim() +
                        ") foi indeferida pelo professor " + marcado_por.getNome() + ".",
                "FALTA",
                idHasher.encode(salvo.getId())
        );

        // 3. RETORNO: Transforma a Entity salva num DTO com Hashes
        return converterParaDto(salvo);
    }

    public void removerFalta(String faltaIdHash) throws Exception {
        Integer idReal = idHasher.decode(faltaIdHash);

        if (!cancelamentoRepository.existsById(idReal)) {
            throw new Exception("Falta não encontrada para remoção.");
        }

        cancelamentoRepository.deleteById(idReal);
    }


    // 1. LISTAR TODAS (Geral)
    public List<FaltaFrontendDto> listarTodas() {
        return cancelamentoRepository.findAll().stream()
                .map(this::converterParaFrontendDto)
                .toList();
    }

    public List<FaltaDto> listarFaltasPorUtilizador(String utilizadorIdHash) {
        // 1. Descodifica o ID
        Integer idReal = idHasher.decode(utilizadorIdHash);

        // 2. Busca os cancelamentos e converte para FaltaDto
        return cancelamentoRepository.findAllByUtilizador_Id(idReal).stream()
                .map(f -> {
                    // Cálculo do estado baseado na lógica que discutimos
                    String estadoCalculado;
                    if (f.getJustificado()) {
                        estadoCalculado = "APROVADA";
                    } else if (f.getJustificadoEm() != null) {
                        estadoCalculado = "INJUSTIFICADA";
                    } else {
                        estadoCalculado = "PENDENTE";
                    }

                    // Retorna o DTO simples
                    return new FaltaDto(
                            idHasher.encode(f.getId()),
                            idHasher.encode(f.getAula().getId()),
                            idHasher.encode(f.getUtilizador().getId()),
                            f.getJustificado(),
                            f.getMotivo(),
                            estadoCalculado
                    );
                }).toList();
    }

    // 3. LISTAR PENDENTES (Para a Coordenação)
    public List<FaltaDto> listarPendentes() {
        return cancelamentoRepository.findByJustificadoFalseAndJustificadoEmNull    ().stream()
                .map(this::converterParaDto)
                .toList();
    }


    public FaltaResumoDto obterResumoEstatisticas(String idHashed) {
        // 1. Executa as queries do repository
        Integer alunoIdReal= idHasher.decode(idHashed);
        long total = cancelamentoRepository.countTotalFaltas(alunoIdReal);
        long justificadas = cancelamentoRepository.countJustificadas(alunoIdReal);
        long pendentes = cancelamentoRepository.countPendentes(alunoIdReal);
        long injustificadas = cancelamentoRepository.countNaoJustificadas(alunoIdReal);
        return new FaltaResumoDto(total, justificadas, pendentes, injustificadas);
    }

    public FaltaDto atualizarFalta(String faltaIdHash, FaltaDto novosDados) throws Exception {
        // 1. Localiza a falta original
        Integer idReal = idHasher.decode(faltaIdHash);
        Cancelamento falta = cancelamentoRepository.findById(idReal)
                .orElseThrow(() -> new Exception("Falta não encontrada."));

        // 2. Atualiza as Entidades Relacionadas (Aula e Aluno)
        if (novosDados.aulaId() != null) {
            Aula novaAula = aulaRepository.findById(idHasher.decode(novosDados.aulaId()))
                    .orElseThrow(() -> new Exception("Aula não encontrada."));
            falta.setAula(novaAula);
        }

        if (novosDados.utilizadorId() != null) {
            Utilizadore novoUtilizador = utilizadoreRepository.findById(idHasher.decode(novosDados.utilizadorId()))
                    .orElseThrow(() -> new Exception("Utilizador não encontrado."));
            falta.setUtilizador(novoUtilizador);
        }

        // 3. Atualiza os campos de estado e texto
        if (novosDados.justificado() != null) {
            falta.setJustificado(novosDados.justificado());
            // Se mudarmos para justificado agora, podemos marcar a data de hoje
            if (novosDados.justificado()) {
                falta.setJustificadoEm(java.time.Instant.now());
            }
        }

        if (novosDados.motivo() != null) {
            falta.setMotivo(novosDados.motivo());
        }

        // 4. Guarda tudo
        Cancelamento salvo = cancelamentoRepository.save(falta);

        return converterParaDto(salvo);
    }


    private FaltaFrontendDto converterParaFrontendDto(Cancelamento c) {

        String estadoCalculado;
        if (c.getJustificado()) {
            estadoCalculado = "APROVADA";
        } else if (c.getJustificadoEm() != null) {
            estadoCalculado = "INJUSTIFICADA";
        } else {
            estadoCalculado = "PENDENTE";
        }

        // Aula completa (reutiliza o converter que já tens para AulaTituloDto)
        AulaTituloDto aulaDto =  converterParaAulaTituloDto(c.getAula());

        // Utilizador que levou a falta
        UtilizadoreResumoDto utilizadorDto = new UtilizadoreResumoDto(
                idHasher.encode(c.getUtilizador().getId()),
                c.getUtilizador().getNome()
        );

        // Quem marcou a falta (pode ser null se foi o sistema)
        UtilizadoreResumoDto marcadoPorDto = c.getMarcardo_por() != null
                ? new UtilizadoreResumoDto(
                idHasher.encode(c.getMarcardo_por().getId()),
                c.getMarcardo_por().getNome())
                : null;
        LocalDateTime ldt = null;
        if(c.getJustificadoEm()!=null) LocalDateTime.ofInstant(c.getJustificadoEm(), ZoneId.systemDefault());

        return new FaltaFrontendDto(
                idHasher.encode(c.getId()),
                aulaDto,
                utilizadorDto,
                c.getJustificado(),
                c.getMotivo(),
                estadoCalculado,
                marcadoPorDto,
                c.getCriadoEm(),
                ldt
        );
    }
// Adicionar estes métodos ao CancelamentoService

    public List<FaltaFrontendDto> listarTodasFrontend() {
        return cancelamentoRepository.findAll().stream()
                .map(this::converterParaFrontendDto)
                .toList();
    }

    public List<FaltaFrontendDto> listarFaltasPorUtilizadorFrontend(String utilizadorIdHash) {
        Integer idReal = idHasher.decode(utilizadorIdHash);
        return cancelamentoRepository.findAllByUtilizador_Id(idReal).stream()
                .map(this::converterParaFrontendDto)
                .toList();
    }

    public List<FaltaFrontendDto> listarPendentesFrontend() {
        return cancelamentoRepository.findByJustificadoFalseAndJustificadoEmNull().stream()
                .map(this::converterParaFrontendDto)
                .toList();
    }

    public List<FaltaFrontendDto> listarFaltasPorProfessorAulaFrontend(String professorIdHash, String aulaId) {
        List<Cancelamento> faltas = cancelamentoRepository.findFaltasByProfessorAula(
                idHasher.decode(professorIdHash), idHasher.decode(aulaId));
        return faltas.stream()
                .map(this::converterParaFrontendDto)
                .toList();
    }

    public List<FaltaFrontendDto> listarFaltasDosEducandosFrontend(String encarregadoId) {
        List<Integer> educandosIds = encarregadoAlunoRepository
                .findAllByEncarregado_Id(idHasher.decode(encarregadoId)).stream()
                .map(ea -> ea.getAluno().getId())
                .toList();

        if (educandosIds.isEmpty()) return List.of();

        return cancelamentoRepository.findByUtilizadorIdIn(educandosIds).stream()
                .map(this::converterParaFrontendDto)
                .toList();
    }
    private AulaTituloDto converterParaAulaTituloDto(Aula aula) {
        String tituloFinal = "Aula";
        Integer maxAlunos = null;
        UtilizadoreResumoDto solicitadoPor = null;

        // Cenário A: Se for uma instância de AulaCoaching
        if (aula instanceof AulaCoaching coaching) {
            if (coaching.getModalidade() != null && coaching.getModalidade().getNome() != null) {
                tituloFinal = "Coaching " + coaching.getModalidade().getNome();
            } else {
                tituloFinal = "Coaching";
            }

            maxAlunos = coaching.getMaxAlunos();

            // Buscar quem pediu (primeiro aluno inscrito)
            AulaAluno aa = aulaAlunoRepository.findFirstByAula_Id(coaching.getId()).orElse(null);
            if (aa != null) {
                solicitadoPor = new UtilizadoreResumoDto(
                        idHasher.encode(aa.getAluno().getId()),
                        aa.getAluno().getNome()
                );
            }
        }
        // Cenário B: Aula regular
        else if (aula.getIdHorario() != null && aula.getIdHorario().getIdturma() != null) {
            var turma = aula.getIdHorario().getIdturma();
            if (turma.getModalidade() != null && turma.getModalidade().getNome() != null) {
                tituloFinal = turma.getModalidade().getNome();
            } else if (turma.getNome() != null) {
                tituloFinal = turma.getNome();
            }
        }

        EstudioDto estudioDto = aula.getEstudio() != null
                ? new EstudioDto(idHasher.encode(aula.getEstudio().getId()), aula.getEstudio().getNome(), aula.getEstudio().getCapacidade(), aula.getEstudio().getNotas())
                : null;

        EstadoAulaDto estadoDto = aula.getEstado() != null
                ? new EstadoAulaDto(idHasher.encode(aula.getEstado().getId()), aula.getEstado().getEstado())
                : null;

        HorarioTurmaDto horarioDto = null;
        if (aula.getIdHorario() != null) {
            // constrói se precisares
        }

        return new AulaTituloDto(
                idHasher.encode(aula.getId()),
                estudioDto,
                aula.getDuracaoMinutos(),
                aula.getDataAula(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                idHasher.encode(aula.getCriadoPor().getId()),
                horarioDto,
                estadoDto,
                tituloFinal,
                maxAlunos,
                solicitadoPor
        );
    }

    private FaltaDto converterParaDto(Cancelamento c) {
        String estadoCalculado = "";
        if (c.getJustificado()) {
            estadoCalculado = "APROVADA";
        } else if (c.getJustificadoEm() != null) {
            // Se justificado é false MAS existe data de processamento, foi rejeitada
            estadoCalculado = "INJUSTIFICADA";
        } else {
            // Se justificado é false E não tem data, ainda ninguém mexeu nela
            estadoCalculado = "PENDENTE";
        }
        return new FaltaDto(
                idHasher.encode(c.getId()),
                idHasher.encode(c.getAula().getId()),
                idHasher.encode(c.getUtilizador().getId()),
                c.getJustificado(),
                c.getMotivo(),
                estadoCalculado
        );
    }
    private String determinarEstadoFalta(Cancelamento f) {
        if (f.getJustificado()) {
            return "JUSTIFICADA";
        }
        // Se não está justificado, mas já tem um motivo escrito (pelo encarregado ou prof)
        if (f.getJustificadoEm() == null) {
            return "PENDENTE";
        }
        return "INJUSTIFICADA";
    }
    public List<FaltaDto> listarFaltasPorProfessor(String professorIdHash) {
        // A query no repositório deve buscar faltas onde a aula pertence ao professorIdHash
        List<Cancelamento> faltas = cancelamentoRepository.findFaltasByProfessor(idHasher.decode(professorIdHash));

        return faltas.stream()
                .map(this::converterParaDto).toList();
    }
    public List<FaltaDto> listarFaltasPorProfessorAula(String professorIdHash,String aulaId) {
        // A query no repositório deve buscar faltas onde a aula pertence ao professorIdHash
        List<Cancelamento> faltas = cancelamentoRepository.findFaltasByProfessorAula(idHasher.decode(professorIdHash),idHasher.decode(aulaId));

        return faltas.stream()
                .map(this::converterParaDto).toList();
    }

    // No CancelamentoService.java

    public List<FaltaDto> listarFaltasDosEducandos(String encarregadoId) {
        // 1. Obter a lista de IDs dos alunos (educandos) associados a este encarregado
        // Exemplo: utilizadorRepository.findEducandosByEncarregadoId(encarregadoId)
        List<Integer> educandosIds = encarregadoAlunoRepository.findAllByEncarregado_Id(idHasher.decode(encarregadoId)).stream().map(encarregadoAluno ->  encarregadoAluno.getAluno().getId()).toList();

        if (educandosIds.isEmpty()) {
            return List.of();
        }

        // 2. Buscar todos os cancelamentos (faltas) desses alunos
        List<Cancelamento> faltas = cancelamentoRepository.findByUtilizadorIdIn(educandosIds);

        // 3. Converter a entidade Cancelamento para FaltaResponseDto
        return faltas.stream()
                .map(falta -> {
                    String es;
                    if(falta.getJustificado()){
                        es="JUSTIFICADA";
                    }else if(falta.getJustificadoEm()==null){
                        es="PENDENTE";
                    }else{
                        es="INJUSTIFICADA";
                    }
                    // Aqui deves usar o teu Mapper ou converter manualmente
                    // para preencher campos como 'disciplina', 'data', 'professor', etc.
                    return new FaltaDto(
                            idHasher.encode(falta.getId()),
                            idHasher.encode(falta.getAula().getId()),
                            null,
                            falta.getJustificado(),
                            falta.getMotivo(),
                            es
                    );
                }).toList()
                ;
    }

    public FaltaResumoDto obterResumoEstatisticasEducandos(String encarregadoIdHash) {
        // 1. Obtém os IDs (já descodificados ou em formato real) dos educandos
        // Assume-se que o utilizadorService já devolve os IDs prontos para a BD
        List<Integer> educandosIds = encarregadoAlunoRepository.findAllByEncarregado_Id(idHasher.decode(encarregadoIdHash)).stream().map(encarregadoAluno ->  encarregadoAluno.getAluno().getId()).toList();

        long totalAcumulado = 0;
        long justificadasAcumuladas = 0;
        long pendentesAcumuladas = 0;
        long injustificadasAcumuladas = 0;

        // 2. Ciclo para somar as estatísticas de cada educando
        for (Integer idReal : educandosIds) {
            long total = cancelamentoRepository.countTotalFaltas(idReal);
            long justificadas = cancelamentoRepository.countJustificadas(idReal);
            long pendentes = cancelamentoRepository.countNaoJustificadas(idReal);
            long injustificadas = cancelamentoRepository.countNaoJustificadas(idReal);

            // Acumula os valores
            totalAcumulado += total;
            justificadasAcumuladas += justificadas;
            pendentesAcumuladas += pendentes;
            injustificadasAcumuladas += injustificadas;
        }

        // 3. Devolve o DTO com os totais somados
        return new FaltaResumoDto(
                totalAcumulado,
                justificadasAcumuladas,
                pendentesAcumuladas,
                injustificadasAcumuladas
        );
    }
}