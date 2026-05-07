package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.EncarregadoAlunoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.UtilizadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/utilizadores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
public class UtilizadorController {

    private final UtilizadorService utilizadorService;
    private final EncarregadoAlunoService encarregadoAlunoService;

    // 1. Gerar o Token
    @PostMapping("/geraTokenEmail")
    public ResponseEntity<?> geraTokenEmail(@RequestParam(name = "email") String email) {
        try {
            utilizadorService.geraToken(email);
            return ResponseEntity.ok("Token enviado para o e-mail");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 2. Alterar a Senha
    @PostMapping("/esqueceuPassword")
    public ResponseEntity<?> esqueceuPassword(@RequestBody AlterarPasswordSemLoginDto dto) {
        try {
            utilizadorService.atualizaPassSemLogin(dto);
            return ResponseEntity.ok("Palavra-passe alterada com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ─── GET /api/utilizadores?tipo=ROLE_ALUNO ────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Page<UtilizadorResponseDto>> listarTodos(
            @RequestParam(name = "tipo", required = false) String tipo,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        try {
            return ResponseEntity.ok(utilizadorService.listarTodos(tipo, pageable));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── GET /api/utilizadores/{id} ───────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> verDetalhe(@PathVariable(name = "id") String id) {
        try {
            return ResponseEntity.ok(utilizadorService.verDetalhe(id));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── POST /api/utilizadores ───────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> criarUtilizador(
            @Valid @RequestBody CriarUtilizadorDto dto) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(utilizadorService.criarUtilizador(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── PATCH /api/utilizadores/{id}/toggle-ativo ────────────────────────────
    @PatchMapping("/{id}/toggle-ativo")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> toggleAtivo(@PathVariable(name = "id") String id) {
        try {
            return ResponseEntity.ok(utilizadorService.toggleAtivo(id));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── DELETE /api/utilizadores/{id} ───────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> apagarUtilizador(@PathVariable(name = "id") String id) {
        try {
            utilizadorService.apagarUtilizador(id);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/eliminaPermanente/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> eliminaUtilizador(@PathVariable(name = "id") String id) {
        try {
            utilizadorService.eliminaUtilizador(id);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── GET /api/utilizadores/meu-perfil ────────────────────────────────────
    @GetMapping("/meu-perfil")
    public ResponseEntity<UtilizadorResponseDto> verMeuPerfil() {
        try {
            return ResponseEntity.ok(
                    utilizadorService.verMeuPerfil(Utils.getAuthenticatedUserId()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── PATCH /api/utilizadores/minha-password ───────────────────────────────
    @PatchMapping("/minha-password")
    public ResponseEntity<?> alterarPalavraPasse(
            @Valid @RequestBody AlterarPasswordDto dto) {
        try {
            utilizadorService.alterarPalavraPasse(Utils.getAuthenticatedUserId(), dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ─── PATCH /api/utilizadores/{id}/repor-password ─────────────────────────
    @PatchMapping("/{id}/repor-password")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> reporPalavraPasse(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody ReporPasswordDto dto) {
        try {
            utilizadorService.reporPalavraPasse(id, dto);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/meus-educandos")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<List<UtilizadoreResumoDto>> educandosEncarregado() {
        try {
            return ResponseEntity.ok(utilizadorService.findEducandosdeEducador(Utils.getAuthenticatedUserId()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/disponiveis-grupo")
    public ResponseEntity<List<UtilizadoreResumoDto>> getUtilizadoresParaGrupo() {
        try {
            String idLogadoHashed = Utils.getAuthenticatedUserId();
            if (idLogadoHashed == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(utilizadorService.listarContactosDisponiveis(idLogadoHashed));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Adicionar
    @PostMapping("/{encarregadoId}/educandos/{alunoId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> adicionarEducando(
            @PathVariable(name = "encarregadoId") String encId,
            @PathVariable(name = "alunoId") String aluId) {
        try {
            encarregadoAlunoService.adicionarEducando(encId, aluId);
            return ResponseEntity.ok().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Remover
    @DeleteMapping("/{encarregadoId}/educandos/{alunoId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> removerEducando(
            @PathVariable(name = "encarregadoId") String encId,
            @PathVariable(name = "alunoId") String aluId) {
        try {
            encarregadoAlunoService.removerEducando(encId, aluId);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── PUT /api/utilizadores/{id}/editar ────────────────────────────────────
    @PutMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> editarUtilizador(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody EditarUtilizadorDto dto) {
        try {
            UtilizadorResponseDto resultado = utilizadorService.editarUtilizador(id, dto);
            return ResponseEntity.ok(resultado);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/associar-aluno-encarregado")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> associarAlunoAEncarregado(
            @RequestParam(name = "idAluno") String idAluno,
            @RequestParam(name = "idEncarregado") String idEncarregado) {
        try {
            utilizadorService.associarAlunoAEncarregado(idAluno, idEncarregado);
            return ResponseEntity.ok("Associação criada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao associar aluno: " + e.getMessage());
        }
    }

    @DeleteMapping("/remover-aluno-encarregado")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> removerAssociacaoAlunoEncarregado(
            @RequestParam(name = "idAluno") String idAluno,
            @RequestParam(name = "idEncarregado") String idEncarregado) {
        try {
            utilizadorService.removerAssociacaoAlunoEncarregado(idAluno, idEncarregado);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao remover associação: " + e.getMessage());
        }
    }
}