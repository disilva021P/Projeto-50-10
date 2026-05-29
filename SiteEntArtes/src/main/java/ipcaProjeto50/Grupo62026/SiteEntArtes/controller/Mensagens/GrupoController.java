package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.GrupoRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadorFiltroGrupoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    /**
     * 1. ENDPOINT PARA O MODAL DE CRIAÇÃO
     * Retorna a lista de utilizadores disponíveis filtrada dinamicamente pelas regras de idade no Java.
     */
    @GetMapping("/disponiveis-grupo")
    public ResponseEntity<?> getUtilizadoresDisponiveis() {
        try {
            // Apanha o ID Hashed do utilizador autenticado a partir do Token JWT
            String utilizadorLogadoHashed = SecurityContextHolder.getContext().getAuthentication().getName();

            // Chama o service passando o ID para o Java aplicar as regras de negócio de 2026
            List<UtilizadorFiltroGrupoDto> listaFiltrada = grupoService.listarUtilizadoresDisponiveisParaGrupo(utilizadorLogadoHashed);

            return ResponseEntity.ok(listaFiltrada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 2. ENDPOINT PARA CARREGAR O CHAT
     * Retorna os membros que JÁ PERTENCEM ao grupo ativo.
     */
    @GetMapping("/{grupoId}/membros")
    public ResponseEntity<?> listarMembros(@PathVariable String grupoId) {
        try {
            var membros = grupoService.listarMembrosDoGrupo(grupoId);
            return ResponseEntity.ok(membros);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 3. ENDPOINT PARA CRIAR O GRUPO PRIVADO
     */
    @PostMapping
    public ResponseEntity<?> criarGrupo(@RequestBody GrupoRequestDto dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userIdHashed = auth.getName();

            // O service cria o grupo e devolve o novo ID em formato Hash String
            String grupoIdHashed = grupoService.criarGrupoPrivado(userIdHashed, dto.nome(), dto.membrosIds());

            // Devolve em formato JSON estruturado para o Axios ler response.data.id sem quebras
            return ResponseEntity.ok(Map.of(
                    "id", grupoIdHashed,
                    "mensagem", "Grupo criado com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 4. ENDPOINT PARA A COORDENAÇÃO ADICIONAR UM MEMBRO À POSTERIORI
     */
    @PutMapping("/{grupoId}/adicionar/{membroId}")
    public ResponseEntity<String> adicionarMembro(
            @PathVariable String grupoId,
            @PathVariable String membroId) {
        try {
            String adminIdHashed = SecurityContextHolder.getContext().getAuthentication().getName();
            grupoService.adicionarMembro(adminIdHashed, grupoId, membroId);
            return ResponseEntity.ok("Membro adicionado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 5. ENDPOINT PARA A COORDENAÇÃO REMOVER UM MEMBRO À POSTERIORI
     */
    @DeleteMapping("/{grupoId}/remover/{membroId}")
    public ResponseEntity<String> removerMembro(
            @PathVariable String grupoId,
            @PathVariable String membroId) {
        try {
            String adminIdHashed = SecurityContextHolder.getContext().getAuthentication().getName();
            grupoService.removerMembro(adminIdHashed, grupoId, membroId);
            return ResponseEntity.ok("Membro removido com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/sou-menor")
    public ResponseEntity<Boolean> verificarSeSouMenor() {
        String utilizadorLogadoHashed = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean ehMenor = grupoService.verificarSeSouMenor(utilizadorLogadoHashed);
        return ResponseEntity.ok(ehMenor);
    }
}