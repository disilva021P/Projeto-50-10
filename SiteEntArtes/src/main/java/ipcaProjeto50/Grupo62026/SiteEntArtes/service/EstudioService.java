package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Estudio;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EstudioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstudioModalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Modalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioModalidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ModalidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EstudioService {
    private final EstudioRepository estudioRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final EstudioModalidadeRepository estudioModalidadeRepository;
    private final IdHasher idHasher;
    private final AulaRepository aulaRepository;

    EstudioDto findEstudioDtobyId(Integer id) throws Exception {
        return converterParaDto(estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado")));
    }
    Estudio findEstudiobyId(Integer id) throws Exception {
        return estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado"));
    }
    public List<EstudioDto> findAll(){
        return estudioRepository.findAll().stream().map(this::converterParaDto).toList();
    }
    // No EstudioService.java

    public EstudioDto findByIdDto(String hashedId) throws Exception {
        Integer id = idHasher.decode(hashedId);
        Estudio estudio = estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado!"));
        return converterParaDto(estudio);
    }

    public EstudioDto create(EstudioDto dto) {
        Estudio estudio = new Estudio();
        estudio.setNome(dto.nome());
        // Outros campos como morada, etc.
        return converterParaDto(estudioRepository.save(estudio));
    }

    public EstudioDto update(String hashedId, EstudioDto dto) throws Exception {
        Integer id = idHasher.decode(hashedId);
        Estudio estudio = estudioRepository.findById(id)
                .orElseThrow(() -> new Exception("Estúdio não encontrado!"));

        estudio.setNome(dto.nome());
        return converterParaDto(estudioRepository.save(estudio));
    }

    public void delete(String hashedId) throws Exception {
        Integer id = idHasher.decode(hashedId);
        estudioRepository.deleteById(id);
    }

    // Método para adicionar modalidade ao estúdio
    public EstudioDto adicionarModalidade(String estudioId, String modalidadeId) throws Exception {
        Estudio estudio = estudioRepository.findById(idHasher.decode(estudioId))
                .orElseThrow(() -> new Exception("Estúdio não encontrado"));

        Modalidade modalidade = modalidadeRepository.findById(idHasher.decode(modalidadeId))
                .orElseThrow(() -> new Exception("Modalidade não encontrada"));

        // Assume que a entidade Estudio tem uma lista: List<Modalidade> modalidades

        estudioModalidadeRepository.save( new EstudioModalidade(null, estudio,modalidade));
        return converterParaDto(estudioRepository.save(estudio));
    }
    public void removerModalidade(String estudioId, String modalidadeId) throws Exception {
        // 1. Descodificar os IDs
        Integer idEstudio = idHasher.decode(estudioId);
        Integer idModalidade = idHasher.decode(modalidadeId);

        // 2. Procurar a associação na tabela intermédia
        // Nota: Deves ter este método definido no teu EstudioModalidadeRepository
        EstudioModalidade associacao = estudioModalidadeRepository.findByEstudio_IdAndModalidade_Id(idEstudio, idModalidade)
                .orElseThrow(() -> new Exception("Esta modalidade não está associada a este estúdio!"));

        // 3. Remover o registo
        estudioModalidadeRepository.delete(associacao);
    }
    EstudioDto converterParaDto(Estudio estudio){
        if(estudio==null) return null;
        return new EstudioDto(idHasher.encode(estudio.getId()),estudio.getNome(),estudio.getCapacidade());
    }
    public List<EstudioDto> buscarEstudioMaisLivre(String modalidadeHashedId) throws Exception {
        Integer modalidadeId = idHasher.decode(modalidadeHashedId);

        // O primeiro da lista será o que tem menos (ou zero) aulas
        List<Estudio> resultados = estudioRepository.findEstudiosMenosOcupados(modalidadeId);

        if (resultados.isEmpty()) {
            throw new Exception("Nenhum estúdio encontrado para esta modalidade.");
        }

        return resultados.stream().map(this::converterParaDto).toList();
    }

    public boolean conflitoestudio(String aulaDtoId) throws Exception {
        Aula aulaDto = aulaRepository.findById(idHasher.decode(aulaDtoId)).orElseThrow(()-> new Exception("Aula nao encontrada"));
        return aulaRepository.existeConflitoNoEstudio(
                aulaDto.getEstudio().getId(),
                aulaDto.getDataAula(),
                aulaDto.getHoraInicio(),
                aulaDto.getHoraFim()
        );

    }
}
