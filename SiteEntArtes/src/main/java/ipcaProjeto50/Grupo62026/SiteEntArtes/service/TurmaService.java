package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TipoPagamentoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaAlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final IdHasher idHasher;
    private final ModalidadeService modalidadeService;
    private final UtilizadoreRepository utilizadoreRepository;
    private final TipoPagamentoRepository tipoPagamentoRepository;
    private final TurmaAlunoRepository turmaAlunoRepository;
    private final PagamentoService pagamentoService;

    TurmaDto findById(Integer id) throws Exception {
        return converterTurmaParaDto(turmaRepository.findById(id).orElseThrow(() -> new Exception("Turma não Encontrada!")));}
    public TurmaDto findById(String id) throws Exception {
        return converterTurmaParaDto(turmaRepository.findById(idHasher.decode(id)).orElseThrow(() -> new Exception("Turma não Encontrada!")));    }
    public List<TurmaDto> findAll(){
        return turmaRepository.findAll().stream().map(this::converterTurmaParaDto).toList();
    }
    // Adicionar ao TurmaService.java

    public TurmaDto create(TurmaDto dto) throws Exception {
        Turma novaTurma = new Turma();
        novaTurma.setNome(dto.nome());
        novaTurma.setMensalidade(dto.mensalidade());

        // Buscar a modalidade usando o Service existente
        if (dto.modalidade() != null && dto.modalidade().id() != null) {
            novaTurma.setModalidade(modalidadeService.findById(dto.modalidade().id()));
        }

        return converterTurmaParaDto(turmaRepository.save(novaTurma));
    }

    public TurmaDto update(String hashedId, TurmaDto dto) throws Exception {
        Turma turmaExistente = turmaRepository.findById(idHasher.decode(hashedId))
                .orElseThrow(() -> new Exception("Turma não encontrada!"));

        turmaExistente.setNome(dto.nome());
        turmaExistente.setMensalidade(dto.mensalidade());

        if (dto.modalidade() != null && dto.modalidade().id() != null) {
            turmaExistente.setModalidade(modalidadeService.findById(dto.modalidade().id()));
        }

        return converterTurmaParaDto(turmaRepository.save(turmaExistente));
    }

    public void delete(String hashedId) throws Exception {
        Integer id = idHasher.decode(hashedId);
        if (!turmaRepository.existsById(id)) {
            throw new Exception("Turma não existe!");
        }
        turmaRepository.deleteById(id);
    }
    TurmaDto converterTurmaParaDto(Turma turma){
        if(turma==null) return null;
        return new TurmaDto(
                idHasher.encode(turma.getId()),
                turma.getNome(),
                turma.getMensalidade(),
                modalidadeService.converterParaDto(turma.getModalidade())
        );
    }
    @Scheduled(cron = "0 0 0 1 * *") // Executa no dia 1 de cada mês
    public void gerarPagamentosMensaisTurmas() {
        // 1. Ir buscar todos os alunos que estão inscritos em turmas
        // Nota: Precisas de um método no teu UtilizadorRepository para isto
        List<TurmaAluno> alunosEmTurmas = turmaAlunoRepository.findAll();

        // 2. Definir o tipo de pagamento ID 1 (ex: "Mensalidade Turma")
        TipoPagamento tipoMensalidade = tipoPagamentoRepository.findById(1)
                .orElse(null);

        for (TurmaAluno turmaaluno : alunosEmTurmas) {
            try {
                String alunoIdHashed = idHasher.encode(turmaaluno.getAluno().getId());
                String tipoPagamentoHashed = idHasher.encode(1);
                UtilizadoreResumoDto resumo = new UtilizadoreResumoDto(
                        alunoIdHashed,
                        turmaaluno.getAluno().getNome()
                );

                // 3. Instanciar o Record (Imutável - valores passados no construtor)
                PagamentoDto novaMensalidade = new PagamentoDto(
                        null,                                        // id (gerado pela DB)
                        turmaaluno.getTurma().getMensalidade(),        // valor (BigDecimal)
                        false,                                       // pago
                        "Mensalidade de " + LocalDate.now().getMonth(), // descricao
                        tipoPagamentoHashed,                         // idTipoPagamento (String)
                        "Mensalidade Turma",                         // tipoPagamentoNome
                        null,                                        // aula (sem aula específica)
                        LocalDate.now(),                             // dataPagamento
                        null,                                        // dataConfirmado
                        resumo                                       // utilizadoreResumoDto
                );
                pagamentoService.criar(novaMensalidade);
            } catch (Exception e) {
                // Log de erro para não interromper o loop dos outros alunos
                System.err.println("Erro ao gerar pagamento para aluno " + turmaaluno.getId() + ": " + e.getMessage());
            }
        }
    }}
