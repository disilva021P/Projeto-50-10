package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResponseDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Cancelamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.CancelamentoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EncarregadoAlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public FaltaDto marcarFalta(FaltaDto faltaDto,String idMarca_por) throws Exception {
        // 1. Descodifica para trabalhar internamente
        Integer idAulaReal = idHasher.decode(faltaDto.aulaId());
        Integer idUserReal = idHasher.decode(faltaDto.utilizadorId());

        Aula aula = aulaRepository.findById(idAulaReal)
                .orElseThrow(()-> new Exception("Aula não encontrada"));

        Utilizadore utilizador = utilizadoreRepository.findById(idUserReal)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));
        Utilizadore marcado_por = utilizadoreRepository.findById(idHasher.decode(idMarca_por))
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        // 2. Guarda na DB
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
                        ") foi indeferida pelo professor " + marcado_por.getNome() + ".", // Mensagem alterada
                "FALTA",
                idHasher.encode( salvo.getId())
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
    public List<FaltaDto> listarTodas() {
        return cancelamentoRepository.findAll().stream()
                .map(this::converterParaDto)
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