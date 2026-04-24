package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TurmaAlunoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Turma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TurmaAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TurmaAlunoId;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaAlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AlunoTurmaService {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaAlunoRepository turmaAlunoRepository;
    private final IdHasher idHasher;

    @Transactional
    public TurmaAlunoDto adicionarAlunoATurma(String idAlunoHashed, String idTurmaHashed) throws Exception {
        Integer idAluno = idHasher.decode(idAlunoHashed);
        Integer idTurma = idHasher.decode(idTurmaHashed);

        TurmaAlunoId id = new TurmaAlunoId();
        id.setAlunoId(idAluno);
        id.setTurmaId(idTurma);

        if (turmaAlunoRepository.existsById(id)) {
            throw new Exception("Aluno já pertence a esta turma");
        }

        Aluno aluno = alunoRepository.findById(idAluno)
                .orElseThrow(() -> new Exception("Aluno não encontrado"));

        TurmaAluno turmaAluno = new TurmaAluno();
        turmaAluno.setId(id);
        turmaAluno.setAluno(aluno);
        turmaAluno.setInscritoEm(LocalDate.now());
        Turma turma = turmaRepository.findById(idTurma)
                .orElseThrow(() -> new Exception("Turma não encontrada"));

        turmaAluno.setTurma(turma);

        return convertToDto(turmaAlunoRepository.save(turmaAluno));
    }

    @Transactional
    public void removerAlunoDaTurma(String idAlunoHashed, String idTurmaHashed) throws Exception {
        TurmaAlunoId id = new TurmaAlunoId();
        id.setAlunoId(idHasher.decode(idAlunoHashed));
        id.setTurmaId(idHasher.decode(idTurmaHashed));

        if (!turmaAlunoRepository.existsById(id)) {
            throw new Exception("Aluno não pertence a esta turma");
        }

        turmaAlunoRepository.deleteById(id);
    }

    public List<TurmaAlunoDto> listarAlunosDaTurma(String idTurmaHashed) {
        return turmaAlunoRepository.findAllByTurma_Id(idHasher.decode(idTurmaHashed)).stream().map(this::convertToDto).toList();
    }
    public TurmaAlunoDto convertToDto(TurmaAluno ta) {
        return new TurmaAlunoDto(
                idHasher.encode(ta.getAluno().getId()),
                idHasher.encode(ta.getTurma().getId()),
                ta.getInscritoEm()
        );
    }
}