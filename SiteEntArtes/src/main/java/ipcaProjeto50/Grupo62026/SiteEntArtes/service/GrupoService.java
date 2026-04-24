package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.GrupoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupoService {
    private final GrupoRepository grupoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;

    @Transactional
    public void criarGrupoPrivado(String idCriadorHashed, String nomeGrupo, List<String> membrosHashedIds) throws Exception {
        // 1. Buscar o criador (usando o ID que vem do sub do token)
        Integer idRealCriador = idHasher.decode(idCriadorHashed);
        Utilizadore criador = utilizadoreRepository.findById(idRealCriador)
                .orElseThrow(() -> new Exception("Criador não encontrado."));

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
                    .orElseThrow(() -> new Exception("Membro não encontrado: " + hashedId));

            int cargoMembro = membro.getTipo().getId();

            // --- VALIDAÇÃO DE REGRAS DE CARGO ---

            // ENCARREGADO (4) -> Só com outros Encarregados (4)
            if (cargoCriador == 4 && cargoMembro != 4) {
                throw new Exception("Como encarregado, só pode criar grupos com outros encarregados.");
            }

            // PROFESSOR (2) -> Com Profs (2), Alunos (3) ou Encarregados (4)
            // Por exclusão: Professor não pode criar grupo com Coordenação (1) nesta lógica
            if (cargoCriador == 2 && cargoMembro == 1) {
                throw new Exception("Professores não podem adicionar membros da coordenação a estes grupos.");
            }

            // ALUNO (3) -> (Opcional) Podes definir se alunos podem criar grupos
            if (cargoCriador == 3) {
                throw new Exception("Alunos não têm permissão para criar grupos privados.");
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


    @Transactional
    public void adicionarMembro(String idAdminHashed, String grupoIdHashed, String novoMembroHashed) throws Exception {
        // 1. Validar se quem está a tentar adicionar é Coordenação (ID 1)
        Integer adminId = idHasher.decode(idAdminHashed);
        Utilizadore admin = utilizadoreRepository.findById(adminId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado."));

        if (admin.getTipo().getId() != 1) {
            throw new Exception("Apenas a coordenação pode editar membros de grupos.");
        }

        // 2. Buscar o grupo e o novo membro
        Grupo grupo = grupoRepository.findById(idHasher.decode(grupoIdHashed))
                .orElseThrow(() -> new Exception("Grupo não encontrado."));

        Utilizadore novoMembro = utilizadoreRepository.findById(idHasher.decode(novoMembroHashed))
                .orElseThrow(() -> new Exception("Utilizador a adicionar não encontrado."));

        // 3. Adicionar se não existir
        if (!grupo.getMembros().contains(novoMembro)) {
            grupo.getMembros().add(novoMembro);
            grupoRepository.save(grupo);
        }
    }

    @Transactional
    public void removerMembro(String idAdminHashed, String grupoIdHashed, String membroARemoverHashed) throws Exception {
        Integer adminId = idHasher.decode(idAdminHashed);
        Utilizadore admin = utilizadoreRepository.findById(adminId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado."));

        if (admin.getTipo().getId() != 1) {
            throw new Exception("Apenas a coordenação pode remover membros.");
        }

        Grupo grupo = grupoRepository.findById(idHasher.decode(grupoIdHashed))
                .orElseThrow(() -> new Exception("Grupo não encontrado."));

        // Evitar que o grupo fique sem o criador se necessário, ou permitir remoção total
        grupo.getMembros().removeIf(m -> idHasher.encode(m.getId()).equals(membroARemoverHashed));

        grupoRepository.save(grupo);
    }

    public List<UtilizadoreResumoDto> listarMembrosDoGrupo(String grupoIdHashed) {
        // 1. Descodificar o ID e procurar o grupo
        Integer idReal = idHasher.decode(grupoIdHashed);
        Grupo grupo = grupoRepository.findById(idReal)
                .orElseThrow(() -> new Exception("Grupo não encontrado."));

        // 2. Converter a lista de entidades Utilizadore para UtilizadoreResumoDto
        // Usamos o Stream para mapear cada membro e codificar o ID de volta para Hash (para o frontend)
        return grupo.getMembros().stream()
                .map(membro -> new UtilizadoreResumoDto(
                        idHasher.encode(membro.getId()),
                        membro.getNome()
                ))
                .collect(Collectors.toList());
    }
}
