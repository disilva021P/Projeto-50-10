package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaCoaching;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaCoachingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AulaCoachingService {

    private final AulaCoachingRepository aulaCoachingRepository;
    private final IdHasher idHasher;
    private final AulaService aulaService;
    private final EstadoAuloService estadoAuloService;
    private final ModalidadeService modalidadeService;
    public AulaCoachingDto findById(Integer id) throws Exception {
        return convertToAulaCoachingDto( aulaCoachingRepository.findById(id).orElseThrow(()-> new Exception("Aula de coaching não encontrada")));
    }
    public AulaCoachingDto findById(String id) throws Exception {
        return convertToAulaCoachingDto( aulaCoachingRepository.findById(idHasher.decode( id)).orElseThrow(()-> new Exception("Aula de coaching não encontrada")));
    }
    public AulaCoachingDto salvar(AulaCoachingDto aulaCoachingDto) throws Exception {
        return aulaCoachingRepository.save(aulaCoachingDto);
    }

    public AulaCoachingDto convertToAulaCoachingDto(AulaCoaching aulaCoaching) throws Exception {
        AulaDto aulaPrincipal = aulaService.bucarPorIdDto(aulaCoaching.getId());
        return new AulaCoachingDto(
                aulaPrincipal,
                aulaCoaching.getMaxAlunos(),
                estadoAuloService.converterParaDto(aulaCoaching.getEstado()),
                modalidadeService.converterParaDto(aulaCoaching.getModalidade())
        );
    }
    public AulaCoaching dtoParacoaching(AulaCoachingDto aulaCoaching){
        Aula aula = aulaService.criarAula(

        )
    }

}