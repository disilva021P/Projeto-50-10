package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EstadoAulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstadoAulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EstadoAuloService {
    private final EstadoAulaRepository estadoAulaRepository;
    private final IdHasher idHasher;

    EstadoAulaDto findbyId(Integer id){
        Optional<EstadoAula> estadoAula = estadoAulaRepository.findById(id);
        return estadoAula.map(this::converterParaDto).orElse(null);
    }
    EstadoAulaDto findbyIdHashed(String id){
        Optional<EstadoAula> estadoAula = estadoAulaRepository.findById(idHasher.decode(id));
        return estadoAula.map(this::converterParaDto).orElse(null);
    }
    EstadoAulaDto converterParaDto(EstadoAula estadoAula){
        if(estadoAula==null) return null;
        return new EstadoAulaDto(
                idHasher.encode(estadoAula.getId()),
                estadoAula.getEstado()
        );
    }
}

