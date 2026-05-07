package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoUtilizador;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.GrupoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

    @Mock private GrupoRepository grupoRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private IdHasher idHasher;

    private GrupoService grupoService;

    @BeforeEach
    void setUp() {
        grupoService = new GrupoService(grupoRepository, utilizadoreRepository, idHasher);
    }

    // --- HELPER PARA CRIAR USERS RAPIDAMENTE ---
    private Utilizadore criarUser(Integer id, int tipoId) {
        Utilizadore u = new Utilizadore();
        u.setId(id);
        u.setNome("User " + id);
        TipoUtilizador t = new TipoUtilizador();
        t.setId(tipoId);
        u.setTipo(t);
        return u;
    }

    // --- TESTES DE CRIAÇÃO (REGRAS DE NEGÓCIO) ---

    @Test
    @DisplayName("Professor deve criar grupo com alunos com sucesso")
    void criarGrupo_ProfessorComAlunos_Sucesso() throws Exception {
        // GIVEN
        Utilizadore prof = criarUser(1, 2); // ID 2 = Professor
        Utilizadore aluno = criarUser(2, 3); // ID 3 = Aluno

        when(idHasher.decode("prof_h")).thenReturn(1);
        when(idHasher.decode("aluno_h")).thenReturn(2);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(prof));
        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(aluno));

        // WHEN
        grupoService.criarGrupoPrivado("prof_h", "Turma A", List.of("aluno_h"));

        // THEN
        verify(grupoRepository).save(argThat(g ->
                g.getNome().equals("Turma A") &&
                        g.getMembros().size() == 2 // Prof + Aluno
        ));
    }

    @Test
    @DisplayName("Deve impedir Aluno de criar grupos")
    void criarGrupo_Aluno_DeveLancarExcecao() {
        // GIVEN
        Utilizadore aluno = criarUser(1, 3); // ID 3 = Aluno
        Utilizadore outro = criarUser(2, 3);

        when(idHasher.decode("aluno_h")).thenReturn(1);
        when(idHasher.decode("membro_h")).thenReturn(2);

        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(aluno));
        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(outro));

        // WHEN & THEN
        // Precisamos de passar pelo menos um membro para o código entrar no loop e validar o cargo
        Exception ex = assertThrows(Exception.class, () ->
                grupoService.criarGrupoPrivado("aluno_h", "Grupo Aluno", List.of("membro_h")));

        assertTrue(ex.getMessage().contains("Alunos não têm permissão"));
    }

    @Test
    @DisplayName("Encarregado só pode adicionar outros encarregados")
    void criarGrupo_EncarregadoComProfessor_DeveLancarExcecao() {
        // GIVEN
        Utilizadore enc = criarUser(1, 4); // 4 = Encarregado
        Utilizadore prof = criarUser(2, 2); // 2 = Professor

        when(idHasher.decode("enc_h")).thenReturn(1);
        when(idHasher.decode("prof_h")).thenReturn(2);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(enc));
        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(prof));

        // WHEN & THEN
        assertThrows(Exception.class, () ->
                grupoService.criarGrupoPrivado("enc_h", "Grupo", List.of("prof_h")));
    }

    // --- TESTES DE ADMINISTRAÇÃO ---

    @Test
    @DisplayName("Coordenação deve adicionar membro a qualquer grupo")
    void adicionarMembro_Coordenacao_Sucesso() throws Exception {
        // GIVEN
        Utilizadore coord = criarUser(1, 1); // 1 = Coordenação
        Utilizadore novoMembro = criarUser(3, 3);
        Grupo grupo = new Grupo();
        grupo.setId(50);
        grupo.setMembros(new ArrayList<>());

        when(idHasher.decode("coord_h")).thenReturn(1);
        when(idHasher.decode("grupo_h")).thenReturn(50);
        when(idHasher.decode("membro_h")).thenReturn(3);

        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(coord));
        when(utilizadoreRepository.findById(3)).thenReturn(Optional.of(novoMembro));
        when(grupoRepository.findById(50)).thenReturn(Optional.of(grupo));

        // WHEN
        grupoService.adicionarMembro("coord_h", "grupo_h", "membro_h");

        // THEN
        assertTrue(grupo.getMembros().contains(novoMembro));
        verify(grupoRepository).save(grupo);
    }

    @Test
    @DisplayName("Deve impedir Professor de adicionar membros via endpoint de admin")
    void adicionarMembro_Professor_DeveLancarExcecao() {
        // GIVEN
        Utilizadore prof = criarUser(1, 2);
        when(idHasher.decode(anyString())).thenReturn(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(prof));

        // WHEN & THEN
        Exception ex = assertThrows(Exception.class, () ->
                grupoService.adicionarMembro("h", "h", "h"));
        assertEquals("Apenas a coordenação pode editar membros de grupos.", ex.getMessage());
    }

    // --- TESTES DE LISTAGEM ---

    @Test
    @DisplayName("Deve listar membros do grupo convertendo para DTO")
    void listarMembrosDoGrupo_Sucesso() throws Exception {
        // GIVEN
        Grupo grupo = new Grupo();
        grupo.setId(1);
        Utilizadore m1 = criarUser(10, 3);
        grupo.setMembros(List.of(m1));

        when(idHasher.decode("h")).thenReturn(1);
        when(grupoRepository.findById(1)).thenReturn(Optional.of(grupo));
        when(idHasher.encode(10)).thenReturn("hash10");

        // WHEN
        List<UtilizadoreResumoDto> resultado = grupoService.listarMembrosDoGrupo("h");

        // THEN
        assertEquals(1, resultado.size());
        assertEquals("User 10", resultado.get(0).nome());
        assertEquals("hash10", resultado.get(0).id());
    }
}