package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final IdHasher idHasher;
    private final ModalidadeService modalidadeService;
    TurmaDto findById(Integer id) throws Exception {
        return converterTurmaParaDto(turmaRepository.findById(id).orElseThrow(() -> new Exception("Turma não Encontrada!")));}
    TurmaDto findById(String id) throws Exception {
        return converterTurmaParaDto(turmaRepository.findById(idHasher.decode(id)).orElseThrow(() -> new Exception("Turma não Encontrada!")));    }

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