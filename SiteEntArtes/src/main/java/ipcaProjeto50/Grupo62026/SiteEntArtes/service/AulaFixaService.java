package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.HorarioTurma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.HorarioFixoRepository;
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
    //TODO: MUDAR PARA DTOS
    private final HorarioFixoRepository horarioFixoRepository;
    private final IdHasher idHasher;

    public PagedModel<HorarioTurma> findAll(Pageable paginacao) {
        Page<HorarioTurma> page = horarioFixoRepository.findAll(paginacao);
        return new PagedModel<>(page);
    }
    public HorarioTurma findById(String id) throws Exception {
        Optional<HorarioTurma> horarioFixo = horarioFixoRepository.findById(idHasher.decode(id));
        return horarioFixo.orElseThrow(() -> new Exception("Horário não encontrado"));
    }
    public List<HorarioTurma>findByIdTurma(String idTurma) throws Exception {
        return horarioFixoRepository.findAllByIdturma_Id(idHasher.decode(idTurma));
    }
    public Map<Turma, List<HorarioTurma>> findHorariosPorTurmas(List<String> idsTurmasHashed) {
        List<Integer> idsDecoded = idsTurmasHashed.stream()
                .map(idHasher::decode)
                .toList();
        List<HorarioTurma> todosOsHorarios = horarioFixoRepository.findAllByIdturma_IdIn(idsDecoded);
        return todosOsHorarios.stream()
                .collect(Collectors.groupingBy(HorarioTurma::getIdturma));
    }

    @Transactional
    public HorarioTurma save(HorarioTurma novoHorario) {
        // Garantimos que o ID está nulo para forçar um INSERT em vez de UPDATE
        novoHorario.setId(null);
        return horarioFixoRepository.save(novoHorario);
    }
    @Transactional
    public HorarioTurma update(String idHashed, HorarioTurma dadosAtualizados) throws Exception {
        Integer idDecoded = idHasher.decode(idHashed);

        // Verificamos se o horário existe antes de tentar atualizar
        return horarioFixoRepository.findById(idDecoded)
                .map(horarioExistente -> {
                    // Atualizamos os campos necessários
                    horarioExistente.setDataInicio(dadosAtualizados.getDataInicio());
                    horarioExistente.setDataValidade(dadosAtualizados.getDataValidade());
                    horarioExistente.setHoraInicio(dadosAtualizados.getHoraInicio());
                    horarioExistente.setHoraFim(dadosAtualizados.getHoraFim());
                    horarioExistente.setDiaSemana(dadosAtualizados.getDiaSemana());
                    horarioExistente.setDuracaoMinutos(dadosAtualizados.getDuracaoMinutos());
                    // Nota: Turma e Criador normalmente não mudam num update de horário,
                    // mas podes adicionar se o teu requisito permitir.
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
}
