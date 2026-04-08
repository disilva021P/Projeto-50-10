package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EstudioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EstudioService {
    private final EstudioRepository estudioRepository;
    private final IdHasher idHasher;
    EstudioDto findEstudioDtobyId(Integer id) throws Exception {
        return converterParaDto(estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado")));
    }
    Estudio findEstudiobyId(Integer id) throws Exception {
        return estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado"));
    }

    EstudioDto converterParaDto(Estudio estudio){
        if(estudio==null) return null;
        return new EstudioDto(idHasher.encode(estudio.getId()),estudio.getNome(),estudio.getCapacidade());
    }

}
