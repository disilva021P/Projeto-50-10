package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EncarregadoAlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EncarregadoAlunoService {
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final AlunoRepository alunoRepository;
    private final IdHasher idHasher; // Se estiveres a usar IDs hasheados no front

    @Transactional
    public void adicionarEducando(String encarregadoIdHashed, String alunoIdHashed) {
        // 1. Decifrar os IDs (se vierem do front-end como hash)
        Integer encId = idHasher.decode(encarregadoIdHashed);
        Integer aluId = idHasher.decode(alunoIdHashed);

        // 2. Buscar as entidades (garante que existem)
        Utilizadore encarregado = utilizadoreRepository.findById(encId)
                .orElseThrow(() -> new EntityNotFoundException("Encarregado não encontrado"));

        Aluno aluno = alunoRepository.findById(aluId)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado"));

        // 3. Criar o ID composto
        EncarregadoAlunoId idComposto = new EncarregadoAlunoId(encId, aluId);

        // 4. Se já existir, não faz nada ou lança erro
        if (encarregadoAlunoRepository.existsById(idComposto)) {
            throw new RuntimeException("Esta relação já existe.");
        }

        // 5. Criar e salvar a relação
        EncarregadoAluno relacao = EncarregadoAluno.builder()
                .id(idComposto)
                .encarregado(encarregado)
                .aluno(aluno)
                .build();

        encarregadoAlunoRepository.save(relacao);
    }

    @Transactional
    public void removerEducando(String encarregadoIdHashed, String alunoIdHashed) {
        Integer encId = idHasher.decode(encarregadoIdHashed);
        Integer aluId = idHasher.decode(alunoIdHashed);

        EncarregadoAlunoId idComposto = new EncarregadoAlunoId(encId, aluId);

        if (!encarregadoAlunoRepository.existsById(idComposto)) {
            throw new EntityNotFoundException("Relação não encontrada.");
        }

        encarregadoAlunoRepository.deleteById(idComposto);
    }
}
