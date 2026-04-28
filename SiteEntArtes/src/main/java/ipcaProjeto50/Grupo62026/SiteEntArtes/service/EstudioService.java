package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EstudioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioModalidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ModalidadeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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
        // 1. Descodificar os IDs que vêm da URL
        Integer idEstudioReal = idHasher.decode(estudioId);
        Integer idModalidadeReal = idHasher.decode(modalidadeId);

        // 2. Validar se ambas as entidades existem
        Estudio estudio = estudioRepository.findById(idEstudioReal)
                .orElseThrow(() -> new Exception("Estúdio não encontrado"));

        Modalidade modalidade = modalidadeRepository.findById(idModalidadeReal)
                .orElseThrow(() -> new Exception("Modalidade não encontrada"));

        // 3. Criar a Chave Composta manualmente
        // Certifica-te que o nome da classe é exatamente este (ou o que definiste)
        EstudioModalidadeId idChave = new EstudioModalidadeId();
        idChave.setEstudioId(idEstudioReal);
        idChave.setModalidadeId(idModalidadeReal);

        // 4. Criar o objeto de ligação
        EstudioModalidade relacao = new EstudioModalidade();
        relacao.setId(idChave); // Define a ID composta
        relacao.setEstudio(estudio);
        relacao.setModalidade(modalidade);

        // 5. Salvar na tabela intermédia
        estudioModalidadeRepository.save(relacao);

        // 6. Retornar o DTO (convertendo a entidade estúdio atualizada)
        return converterParaDto(estudio);
    }
    @Transactional // Importante para operações de remoção
    public void removerModalidade(String estudioId, String modalidadeId) throws Exception {
        // 1. Descodificar os IDs
        Integer idEstudioReal = idHasher.decode(estudioId);
        Integer idModalidadeReal = idHasher.decode(modalidadeId);

        // 2. Criar a ID Composta
        EstudioModalidadeId idChave = new EstudioModalidadeId();
        idChave.setEstudioId(idEstudioReal);
        idChave.setModalidadeId(idModalidadeReal);

        // 3. Verificar se existe antes de apagar (para lançar a exceção correta)
        if (!estudioModalidadeRepository.existsById(idChave)) {
            throw new Exception("Esta modalidade não está associada a este estúdio!");
        }

        // 4. Apagar diretamente pela ID
        estudioModalidadeRepository.deleteById(idChave);
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
    public boolean conflitoestudio(Integer estudioId, LocalDate data, LocalTime inicio, LocalTime fim) {
        return aulaRepository.existeConflitoNoEstudio(
                estudioId,
                data,
                inicio,
                fim
        );
    }
}