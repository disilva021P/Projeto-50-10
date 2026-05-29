package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadorFiltroGrupoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.GrupoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public String criarGrupoPrivado(String idCriadorHashed, String nomeGrupo, List<String> membrosHashedIds) throws Exception {
        Integer idRealCriador = idHasher.decode(idCriadorHashed);
        Utilizadore criador = utilizadoreRepository.findById(idRealCriador)
                .orElseThrow(() -> new Exception("Criador não encontrado."));

        if (ehAlunoMenor(criador)) {
            throw new Exception("Alunos menores de idade não têm permissão para criar grupos.");
        }

        Set<Utilizadore> membrosSet = new HashSet<>();
        membrosSet.add(criador);

        for (String hashedId : membrosHashedIds) {
            Integer idMembroReal = idHasher.decode(hashedId);
            if (idMembroReal.equals(idRealCriador)) continue;

            Utilizadore membro = utilizadoreRepository.findById(idMembroReal)
                    .orElseThrow(() -> new Exception("Membro não encontrado: " + hashedId));

            validarMembroPermitido(criador, membro);
            membrosSet.add(membro);
        }

        Grupo novoGrupo = new Grupo();
        novoGrupo.setNome(nomeGrupo);
        novoGrupo.setCriador(criador);
        novoGrupo.setMembros(new ArrayList<>(membrosSet));

        Grupo grupoSalvo = grupoRepository.save(novoGrupo);
        return idHasher.encode(grupoSalvo.getId());
    }

    @Transactional
    public void adicionarMembro(String idUtilizadorHashed, String grupoIdHashed, String novoMembroHashed) throws Exception {
        Integer utilizadorId = idHasher.decode(idUtilizadorHashed);

        Utilizadore utilizador = utilizadoreRepository.findById(utilizadorId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado."));

        Grupo grupo = grupoRepository.findById(idHasher.decode(grupoIdHashed))
                .orElseThrow(() -> new Exception("Grupo não encontrado."));

        if (!podeGerirGrupo(utilizador, grupo)) {
            throw new Exception("Apenas a coordenação ou o criador do grupo pode editar membros.");
        }

        Utilizadore novoMembro = utilizadoreRepository.findById(idHasher.decode(novoMembroHashed))
                .orElseThrow(() -> new Exception("Utilizador a adicionar não encontrado."));

        validarMembroPermitido(utilizador, novoMembro);

        if (!grupo.getMembros().contains(novoMembro)) {
            grupo.getMembros().add(novoMembro);
            grupoRepository.save(grupo);
        }
    }

    @Transactional
    public void removerMembro(String idUtilizadorHashed, String grupoIdHashed, String membroARemoverHashed) throws Exception {
        Integer utilizadorId = idHasher.decode(idUtilizadorHashed);

        Utilizadore utilizador = utilizadoreRepository.findById(utilizadorId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado."));

        Grupo grupo = grupoRepository.findById(idHasher.decode(grupoIdHashed))
                .orElseThrow(() -> new Exception("Grupo não encontrado."));

        if (!podeGerirGrupo(utilizador, grupo)) {
            throw new Exception("Apenas a coordenação ou o criador do grupo pode remover membros.");
        }

        Integer membroId = idHasher.decode(membroARemoverHashed);

        if (grupo.getCriador() != null && grupo.getCriador().getId().equals(membroId)) {
            throw new Exception("O criador do grupo não pode ser removido.");
        }

        grupo.getMembros().removeIf(m -> m.getId().equals(membroId));
        grupoRepository.save(grupo);
    }

    public List<UtilizadorFiltroGrupoDto> listarUtilizadoresDisponiveisParaGrupo(String idUtilizadorLogadoHashed) {
        Integer idLogadoReal = idHasher.decode(idUtilizadorLogadoHashed);

        Utilizadore utilizadorLogado = utilizadoreRepository.findById(idLogadoReal).orElse(null);
        if (utilizadorLogado == null) return new ArrayList<>();

        int cargoLogado = utilizadorLogado.getTipo().getId();

        return utilizadoreRepository.findAll().stream()
                .filter(u -> {
                    if (u.getId().equals(idLogadoReal)) return false;

                    int cargoAlvo = u.getTipo().getId();
                    boolean alvoEhMenor = ehAlunoMenor(u);

                    if (cargoLogado == 1) return true;
                    if (cargoLogado == 2) return cargoAlvo != 1;

                    if (cargoLogado == 3 || cargoLogado == 4) {
                        return cargoAlvo != 1 && cargoAlvo != 2 && !alvoEhMenor;
                    }

                    return false;
                })
                .map(u -> new UtilizadorFiltroGrupoDto(
                        idHasher.encode(u.getId()),
                        u.getNome(),
                        u.getDataNascimento()
                ))
                .collect(Collectors.toList());
    }

    public List<UtilizadoreResumoDto> listarMembrosDoGrupo(String grupoIdHashed) throws Exception {
        Integer grupoId = idHasher.decode(grupoIdHashed);
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new Exception("Grupo não encontrado."));

        return grupo.getMembros().stream()
                .map(m -> new UtilizadoreResumoDto(
                        idHasher.encode(m.getId()),
                        m.getNome()
                ))
                .collect(Collectors.toList());
    }

    public boolean verificarSeSouMenor(String idUtilizadorHashed) {
        try {
            Integer idReal = idHasher.decode(idUtilizadorHashed);
            Utilizadore u = utilizadoreRepository.findById(idReal).orElse(null);
            return u != null && ehAlunoMenor(u);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean podeGerirGrupo(Utilizadore utilizador, Grupo grupo) {
        boolean ehCoordenacao = utilizador.getTipo().getId() == 1;
        boolean ehCriador = grupo.getCriador() != null
                && grupo.getCriador().getId().equals(utilizador.getId());

        return ehCoordenacao || ehCriador;
    }

    private void validarMembroPermitido(Utilizadore gestor, Utilizadore membro) throws Exception {
        int cargoGestor = gestor.getTipo().getId();
        int cargoMembro = membro.getTipo().getId();
        boolean membroEhMenor = ehAlunoMenor(membro);

        if (cargoGestor == 1) return;

        if (cargoGestor == 2 && cargoMembro == 1) {
            throw new Exception("Professores não podem adicionar membros da coordenação.");
        }

        if (cargoGestor == 3 && (cargoMembro == 1 || cargoMembro == 2 || membroEhMenor)) {
            throw new Exception("Alunos maiores só podem criar grupos com Encarregados ou outros Alunos maiores de idade.");
        }

        if (cargoGestor == 4 && (cargoMembro == 1 || cargoMembro == 2 || membroEhMenor)) {
            throw new Exception("Encarregados só podem criar grupos com Alunos maiores de idade ou outros Encarregados.");
        }
    }

    private boolean ehAlunoMenor(Utilizadore utilizador) {
        return utilizador.getTipo().getId() == 3
                && utilizador.getDataNascimento() != null
                && LocalDate.now().getYear() - utilizador.getDataNascimento().getYear() < 18;
    }
}