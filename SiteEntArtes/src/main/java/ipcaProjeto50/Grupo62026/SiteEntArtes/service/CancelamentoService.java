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
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelamentoService {
    private final CancelamentoRepository cancelamentoRepository;
    private final AulaRepository aulaRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;

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

        Cancelamento salvo = cancelamentoRepository.save(cancelamento);

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

    public List<FaltaResponseDto> listarFaltasPorUtilizador(String utilizadorIdHash) {
        // 1. Descodifica o ID (pode ser Aluno ou Professor)
        Integer idReal = idHasher.decode(utilizadorIdHash);

        // 2. Busca na tabela de cancelamentos onde o 'utilizador_id' coincide
        return cancelamentoRepository.findByUtilizadorId(idReal).stream()
                .map(f -> {
                    // Procuramos saber quem marcou a falta (Coordenador ou Diretor)
                    String nomeQuemMarcou = (f.getMarcardo_por() != null) ? f.getMarcardo_por().getNome() : "Sistema";

                    return new FaltaResponseDto(
                            idHasher.encode(f.getId()),
                            f.getAula().getDataAula().toString(),
                            f.getAula().getDataAula().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "PT")),
                            f.getAula().getHoraInicio() + " - " + f.getAula().getHoraFim(),
                            f.getAula().getIdHorario().getIdturma().getModalidade().getNome(),
                            determinarEstadoFalta(f),
                            nomeQuemMarcou, // Aqui aparece o nome do Coordenador que marcou a falta ao Professor
                            f.getMotivo()
                    );
                }).toList();
    }

    // 3. LISTAR PENDENTES (Para a Coordenação)
    public List<FaltaDto> listarPendentes() {
        return cancelamentoRepository.findByJustificadoFalse().stream()
                .map(this::converterParaDto)
                .toList();
    }

    public FaltaResumoDto obterResumoEstatisticas(String alunoIdHash) {
        Integer idReal = idHasher.decode(alunoIdHash);

        long total = cancelamentoRepository.countTotalFaltas(idReal);
        long justificadas = cancelamentoRepository.countJustificadas(idReal);
        long pendentes = cancelamentoRepository.countNaoJustificadas(idReal);

        // Exemplo: Injustificadas podes calcular pela diferença ou outra query
        long injustificadas = total - justificadas - pendentes;

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
        return new FaltaDto(
                idHasher.encode(c.getId()),
                idHasher.encode(c.getAula().getId()),
                idHasher.encode(c.getUtilizador().getId()),
                c.getJustificado(),
                c.getMotivo()
        );
    }
    private String determinarEstadoFalta(Cancelamento f) {
        if (f.getJustificado()) {
            return "JUSTIFICADA";
        }
        // Se não está justificado mas já tem um motivo escrito (pelo encarregado ou prof)
        if (f.getMotivo() != null && !f.getMotivo().isEmpty()) {
            return "POR ANALISAR";
        }
        return "INJUSTIFICADA";
    }
}