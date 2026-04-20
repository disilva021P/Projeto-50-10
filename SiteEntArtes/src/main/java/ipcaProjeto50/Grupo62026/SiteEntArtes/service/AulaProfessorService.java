package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaProfessorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaProfessore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaProfessoreId;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Professore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaProfessoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AulaProfessorService {

    private final AulaProfessoreRepository aulaProfessoreRepository;
    private final IdHasher idHasher;
    private final AulaRepository aulaRepository;
    private final ProfessorService professorService;

    // CREATE
    @Transactional
    public AulaProfessorDto save(AulaProfessorDto dto) throws Exception {
        // 1. Descodificar os IDs para buscar as entidades reais
        Aula aula = aulaRepository.findById(idHasher.decode( dto.idAula())).orElseThrow(()-> new Exception("Aula não encontrada"));
        Professore professor = professorService.findById(dto.idProfessor());

        // 2. Criar a chave composta
        AulaProfessoreId id = new AulaProfessoreId(aula.getId(), professor.getId());

        // 3. Instanciar a entidade de associação
        AulaProfessore aulaProfessore = new AulaProfessore();
        aulaProfessore.setId(id);
        aulaProfessore.setAula(aula);
        aulaProfessore.setProfessor(professor);

        // 4. Salvar e retornar como DTO
        AulaProfessore salvo = aulaProfessoreRepository.save(aulaProfessore);
        return convertoToDto(salvo);
    }

    // READ (Todos)
    public List<AulaProfessorDto> findAll() {
        return aulaProfessoreRepository.findAll()
                .stream()
                .map(this::convertoToDto)
                .toList();
    }

    // READ (Por Aula)
    public List<AulaProfessorDto> findByAulaId(String hashId) {
        Integer realId = idHasher.decode(hashId);
        return aulaProfessoreRepository.findByAula_Id(realId)
                .stream()
                .map(this::convertoToDto)
                .toList();
    }

    // READ (Por Professor)
    public List<AulaProfessorDto> findByProfessorId(String hashId) {
        Integer realId = idHasher.decode(hashId);
        return aulaProfessoreRepository.findByProfessor_Id(realId)
                .stream()
                .map(this::convertoToDto)
                .toList();
    }

    // DELETE
    @Transactional
    public void deleteById(String hashIdProfessor, String hashIdAula) {
        // Criamos a chave composta com os IDs descodificados para apagar o registo exato
        AulaProfessoreId id = new AulaProfessoreId(
                idHasher.decode(hashIdAula),
                idHasher.decode(hashIdProfessor)
        );
        aulaProfessoreRepository.deleteById(id);
    }

    // MAPPING
    public AulaProfessorDto convertoToDto(AulaProfessore a) {
        return new AulaProfessorDto(
                idHasher.encode(a.getProfessor().getId()),
                idHasher.encode(a.getAula().getId())
        );
    }

}