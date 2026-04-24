package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.HorarioFixoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AulaFixaService {
    private final HorarioFixoRepository horarioFixoRepository;
    private final IdHasher idHasher;
    private final UtilizadoreRepository utilizadoreRepository;
    private final TurmaService turmaService;
    private final EstudioRepository estudioRepository;
    private final TurmaRepository turmaRepository;
    private final ModalidadeService modalidadeService;
    private final EstudioService estudioService;
    private final UtilizadorService utilizadorService;

    public PagedModel<HorarioTurmaDto> findAll(Pageable paginacao) {
        Page<HorarioTurmaDto> page = horarioFixoRepository.findAll(paginacao).map(this::convertToDto);
        return new PagedModel<>(page);
    }
    public HorarioTurmaDto findById(String id) throws Exception {
        Optional<HorarioTurma> horarioFixo = horarioFixoRepository.findById(idHasher.decode(id));
        return convertToDto(horarioFixo.orElseThrow(() -> new Exception("Horário não encontrado")));
    }
    public List<HorarioTurmaDto>findByIdTurma(String idTurma) throws Exception {
        return horarioFixoRepository.findAllByIdturma_Id(idHasher.decode(idTurma)).stream().map(this::convertToDto).toList();
    }
    public Map<TurmaDto, List<HorarioTurmaDto>> findHorariosPorTurmas(List<String> idsTurmasHashed) {
        List<Integer> idsDecoded = idsTurmasHashed.stream()
                .map(idHasher::decode)
                .toList();
        List<HorarioTurmaDto> todosOsHorarios = horarioFixoRepository.findAllByIdturma_IdIn(idsDecoded).stream().map(this::convertToDto).toList();
        return todosOsHorarios.stream()
                .collect(Collectors.groupingBy(HorarioTurmaDto::idturmaId));
    }

    @Transactional
    public HorarioTurma save(HorarioTurmaDto novoHorario) throws Exception {
        return horarioFixoRepository.save(fromDtoToHorarioTurma( novoHorario));
    }
    @Transactional
    public HorarioTurma update(String idHashed, HorarioTurmaDto dadosAtualizados) throws Exception {
        Integer idDecoded = idHasher.decode(idHashed);

        // Verificamos se o horário existe antes de tentar atualizar
        return horarioFixoRepository.findById(idDecoded)
                .map(horarioExistente -> {
                    horarioExistente.setDataInicio(dadosAtualizados.dataInicio());
                    horarioExistente.setDataValidade(dadosAtualizados.dataValidade());
                    horarioExistente.setHoraInicio(dadosAtualizados.horaInicio());
                    horarioExistente.setHoraFim(dadosAtualizados.horaFim());
                    horarioExistente.setDiaSemana(dadosAtualizados.diaSemana());
                    horarioExistente.setDuracaoMinutos(dadosAtualizados.duracaoMinutos());
                    try {
                        horarioExistente.setEstudioId(estudioService.findEstudiobyId(idHasher.decode(dadosAtualizados.estudioId().id())));
                    } catch (Exception e) {
                        throw new Exception("Estudio não encontrado");
                    }
                    // Nota: Turma e Criador normalmente não mudam num update de horário,
                    return horarioFixoRepository.save(horarioExistente);
                })
                .orElseThrow(() -> new Exception("Horário não encontrado para atualização"));
    }
    @Transactional
    public void delete(String idHashed) throws Exception {
        Integer idDecoded = idHasher.decode(idHashed);

        if (!horarioFixoRepository.existsById(idDecoded)) {
            throw new Exception("Não foi possível remover: Horário não encontrado");
        }

        horarioFixoRepository.deleteById(idDecoded);
    }
    @Transactional
    public void delete(Integer id) throws Exception {

        if (!horarioFixoRepository.existsById(id)) {
            throw new Exception("Não foi possível remover: Horário não encontrado");
        }

        horarioFixoRepository.deleteById(id);
    }
    public HorarioTurma fromDtoToHorarioTurma(HorarioTurmaDto h) throws Exception {
        if (h==null) return null;
        Utilizadore utilizadore = utilizadoreRepository.findById(idHasher.decode(h.idcriadoPor().id())).orElseThrow(()-> new Exception("Erro a encontrar utilizador"));
        Turma turma = turmaRepository.findById(idHasher.decode(h.idturmaId().id())).orElseThrow(()-> new Exception("Erro a encontrar turma"));
        Estudio estudio = estudioRepository.findById(idHasher.decode(h.estudioId().id())).orElseThrow(()-> new Exception("Erro a encontrar estudio"));
        return new HorarioTurma(
                null,
                utilizadore,
                turma,
                h.dataInicio(),
                h.dataValidade(),
                h.diaSemana(),
                h.duracaoMinutos(),
                h.horaInicio(),
                h.horaFim(),
                estudio
        );
    }
    public HorarioTurmaDto convertToDto(HorarioTurma horarioTurma){
        if(horarioTurma==null) return null;
        return new HorarioTurmaDto(
                idHasher.encode(horarioTurma.getId()),
                new UtilizadoreResumoDto(idHasher.encode(horarioTurma.getCriadoPor().getId()),horarioTurma.getCriadoPor().getNome()),
                turmaService.converterTurmaParaDto(horarioTurma.getIdturma()),
                horarioTurma.getDataInicio(),
                horarioTurma.getDataValidade(),
                horarioTurma.getDiaSemana(),
                horarioTurma.getDuracaoMinutos(),
                horarioTurma.getHoraInicio(),
                horarioTurma.getHoraFim(),
                estudioService.converterParaDto(horarioTurma.getEstudioId())
        );
    }
    public HorarioTurmaDto convertRequestToDto(HorarioTurmaRequestDto horarioTurmaRequestDto) throws Exception {
        if (horarioTurmaRequestDto==null) return null;
        UtilizadorResponseDto u = utilizadorService.verDetalhe(horarioTurmaRequestDto.idcriadoPor());
        return new HorarioTurmaDto(
                horarioTurmaRequestDto.id(),
                new UtilizadoreResumoDto(u.id(),u.nome()),
                turmaService.findById(horarioTurmaRequestDto.idturma()),
                horarioTurmaRequestDto.dataInicio(),
                horarioTurmaRequestDto.dataValidade(),
                horarioTurmaRequestDto.diaSemana(),
                horarioTurmaRequestDto.duracaoMinutos(),
                horarioTurmaRequestDto.horaInicio(),
                horarioTurmaRequestDto.horaFim(),
                estudioService.findEstudioDtobyId(idHasher.decode(horarioTurmaRequestDto.estudioId()))


        );
    }
}
