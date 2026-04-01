package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EstudioService {
    private final EstudioRepository estudioRepository;
    Estudio findEstudiobyId(Integer id) throws Exception {
        return estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado"));    }

}
