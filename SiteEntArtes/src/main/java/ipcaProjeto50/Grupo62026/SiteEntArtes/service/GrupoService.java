package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.GrupoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GrupoService {
    private final GrupoRepository grupoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;

    @Transactional
    public void criarGrupoPrivado(String idCriadorHashed, String nomeGrupo, List<String> membrosHashedIds) {
        // 1. Buscar o criador (usando o ID que vem do sub do token)
        Integer idRealCriador = idHasher.decode(idCriadorHashed);
        Utilizadore criador = utilizadoreRepository.findById(idRealCriador)
                .orElseThrow(() -> new RuntimeException("Criador não encontrado."));

        // 2. Identificar o cargo do criador
        // IDs: 1=Coordenação, 2=Professor, 3=Aluno, 4=Encarregado
        int cargoCriador = criador.getTipo().getId();

        // 3. Iniciar a lista de membros (Set evita duplicados automaticamente)
        Set<Utilizadore> membrosSet = new HashSet<>();
        membrosSet.add(criador);

        // 4. Processar e Validar cada membro selecionado
        for (String hashedId : membrosHashedIds) {
            Integer idMembroReal = idHasher.decode(hashedId);

            // Regra 1: Se o ID for igual ao do criador, ignoramos (evita auto-adição duplicada)
            if (idMembroReal.equals(idRealCriador)) continue;

            Utilizadore membro = utilizadoreRepository.findById(idMembroReal)
                    .orElseThrow(() -> new RuntimeException("Membro não encontrado: " + hashedId));

            int cargoMembro = membro.getTipo().getId();

            // --- VALIDAÇÃO DE REGRAS DE CARGO ---

            // ENCARREGADO (4) -> Só com outros Encarregados (4)
            if (cargoCriador == 4 && cargoMembro != 4) {
                throw new RuntimeException("Como encarregado, só pode criar grupos com outros encarregados.");
            }

            // PROFESSOR (2) -> Com Profs (2), Alunos (3) ou Encarregados (4)
            // Por exclusão: Professor não pode criar grupo com Coordenação (1) nesta lógica
            if (cargoCriador == 2 && cargoMembro == 1) {
                throw new RuntimeException("Professores não podem adicionar membros da coordenação a estes grupos.");
            }

            // ALUNO (3) -> (Opcional) Podes definir se alunos podem criar grupos
            if (cargoCriador == 3) {
                throw new RuntimeException("Alunos não têm permissão para criar grupos privados.");
            }

            // COORDENAÇÃO (1) -> Não entra em if nenhum, logo tem acesso total.

            membrosSet.add(membro);
        }

        // 5. Salvar o Grupo
        Grupo novoGrupo = new Grupo();
        novoGrupo.setNome(nomeGrupo);
        novoGrupo.setCriador(criador);

        // Converter Set para List para a entidade
        novoGrupo.setMembros(new ArrayList<>(membrosSet));

        grupoRepository.save(novoGrupo);
    }
}
