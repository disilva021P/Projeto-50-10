package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Modalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ModalidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ModalidadeService {
    private final IdHasher idHasher;
    private final ModalidadeRepository modalidadeRepository;
    ModalidadeDto findbyId(Integer id) throws Exception {
        Optional<Modalidade> modalidade = modalidadeRepository.findById(id);
        return converterParaDto(modalidade.orElseThrow(() -> new Exception("Não existe nenhum!")));
    }
    ModalidadeDto findbyId(String id) throws Exception {
        Optional<Modalidade> modalidade = modalidadeRepository.findById(idHasher.decode(id));
        return converterParaDto(modalidade.orElseThrow(() -> new Exception("Não existe nenhum!")));
    }
    ModalidadeDto converterParaDto(Modalidade modalidade){
        if (modalidade==null){
            return null;
        }
        return new ModalidadeDto(idHasher.encode(modalidade.getId()),modalidade.getNome());
    }
}