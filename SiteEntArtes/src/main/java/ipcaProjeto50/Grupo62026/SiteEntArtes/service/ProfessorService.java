package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessoreDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Professore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ProfessoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfessorService {
    private final IdHasher idHasher;
    private final ProfessoreRepository professoreRepository;
    public Professore findById(Integer id) throws Exception {
        return professoreRepository.findById(id).orElseThrow(()-> new Exception("Professor não encontrado"));
    }
    public Professore findById(String id) throws Exception {
        return professoreRepository.findById(idHasher.decode(id)).orElseThrow(()-> new Exception("Professor não encontrado"));
    }
    public List<ProfessoreDto> findAll(){
        return professoreRepository.findAll().stream().map(this::convertToDto).toList();
    }
    public ProfessoreDto convertToDto(Professore p){
        return p == null ? null : new ProfessoreDto(
                new UtilizadoreResumoDto(idHasher.encode(p.getId()),p.getUtilizadores().getNome() ),
                p.getValorHora(),
                p.getProfessorExterno()
        );
    }
}
