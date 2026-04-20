package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final IdHasher idHasher;
    private final ModalidadeService modalidadeService;
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
}
