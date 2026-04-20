package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaAlunoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaAlunoId;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaAlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AlunoRepository; // Injetado aqui
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AulaAlunoService {

    private final AulaAlunoRepository aulaAlunoRepository;
    private final IdHasher idHasher;
    private final AulaRepository aulaRepository;
    private final AlunoRepository alunoRepository; // Substituiu o AlunoService

    // CREATE
    @Transactional
    public AulaAluno save(AulaAlunoDto dto) throws Exception {
        // 1. Descodificar e buscar a Aula
        Aula aula = aulaRepository.findById(idHasher.decode(dto.idAula()))
                .orElseThrow(() -> new Exception("Aula não encontrada"));

        // 2. Descodificar e buscar o Aluno diretamente pelo Repository
        Integer idAlunoReal = idHasher.decode(dto.idAluno());
        Aluno aluno = alunoRepository.findById(idAlunoReal)
                .orElseThrow(() -> new Exception("Aluno não encontrado"));

        // 3. Instanciar a entidade de associação
        // O construtor personalizado já cria   o AulaAlunoId internamente
        AulaAluno aulaAluno = new AulaAluno(aula, aluno);

        // 4. Salvar e retornar como DTO
        return aulaAlunoRepository.save(aulaAluno);
    }

    // READ (Todos)
    public List<AulaAlunoDto> findAll() {
        return aulaAlunoRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // READ (Por Aula)
    public List<AulaAlunoDto> findByAulaId(String hashId) {
        Integer realId = idHasher.decode(hashId);
        return aulaAlunoRepository.findByAula_Id(realId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // READ (Por Aluno)
    public List<AulaAlunoDto> findByAlunoId(String hashId) {
        Integer realId = idHasher.decode(hashId);
        return aulaAlunoRepository.findByAluno_Id(realId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // DELETE
    @Transactional
    public void deleteById(String hashIdAluno, String hashIdAula) {
        AulaAlunoId id = new AulaAlunoId(
                idHasher.decode(hashIdAula),
                idHasher.decode(hashIdAluno)
        );
        aulaAlunoRepository.deleteById(id);
    }

    // MAPPING
    public AulaAlunoDto convertToDto(AulaAluno a) {
        return new AulaAlunoDto(
                idHasher.encode(a.getAluno().getId()),
                idHasher.encode(a.getAula().getId())
        );
    }
}