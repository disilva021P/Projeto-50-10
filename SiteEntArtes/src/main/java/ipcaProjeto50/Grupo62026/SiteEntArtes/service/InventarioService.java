package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioAdicionarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioEditarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class InventarioService {

    private final InventarioUnidadeRepository inventarioUnidadeRepository;
    private final ImagensUnidadeRepository imagensUnidadeRepository;
    private final EstadoUnidadeRepository estadoUnidadeRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;

    public Page<InventarioDto> filtrarInventario(
            String nome, Integer estadoId, String tamanho,
            String cor, String condicao, Pageable pageable) {

        return inventarioUnidadeRepository
                .filtrarInventario(nome, estadoId, tamanho, cor, condicao, pageable)
                .map(this::toDto);
    }

    private InventarioDto toDto(InventarioUnidade u) {
        // Como o inventário agora é independente, os campos de "vestuário" e imagens
        // são passados como null/vazios para manter a compatibilidade com o DTO
        return new InventarioDto(
                idHasher.encode(u.getId()),
                u.getNome(),
                u.getDescricao(),
                null,
                null,
                null,
                u.getEstado().getId(),
                u.getEstado().getEstado(),
                u.getDisponivel(),
                u.getLocalizacao(),
                u.getNotas(),
                u.getCriadoEm(),
                null,
                List.of()
        );
    }

    @Transactional
    public void removerDoInventario(String unidadeIdHashed) {
        Integer idOriginal = idHasher.decode(unidadeIdHashed);
        if (!inventarioUnidadeRepository.existsById(idOriginal)) {
            throw new RuntimeException("Unidade não encontrada");
        }
        inventarioUnidadeRepository.deleteById(idOriginal);
    }

    @Transactional
    public InventarioDto editarUnidade(String unidadeIdHashed, InventarioEditarRequest request) {
        Integer idOriginal = idHasher.decode(unidadeIdHashed);
        InventarioUnidade unidade = inventarioUnidadeRepository.findById(idOriginal)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        // Atualiza campos básicos da própria unidade
        if (request.nome() != null) unidade.setNome(request.nome());
        if (request.descricao() != null) unidade.setDescricao(request.descricao());
        if (request.disponivel() != null) unidade.setDisponivel(request.disponivel());
        if (request.localizacao() != null) unidade.setLocalizacao(request.localizacao());
        if (request.notas() != null) unidade.setNotas(request.notas());

        return toDto(inventarioUnidadeRepository.save(unidade));
    }

    @Transactional
    public InventarioDto adicionarAoInventario(InventarioAdicionarRequest request) {

        // Definir o estado (Default 9 - Inventário conforme a sua tabela)
        EstadoUnidade estado = estadoUnidadeRepository.findById(
                        request.estadoId() != null ? request.estadoId() : 9)
                .orElseThrow(() -> new RuntimeException("Estado não encontrado"));

        // Criar a Unidade diretamente (Sem criar Artigo)
        InventarioUnidade unidade = new InventarioUnidade();
        unidade.setNome(request.nome());
        unidade.setDescricao(request.descricao());
        unidade.setEstado(estado);
        unidade.setDisponivel(request.disponivel() != null ? request.disponivel() : true);
        unidade.setLocalizacao(request.localizacao());
        unidade.setNotas(request.notas());
        unidade.setCriadoEm(Instant.now());

        return toDto(inventarioUnidadeRepository.save(unidade));
    }
}