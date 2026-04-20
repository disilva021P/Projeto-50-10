package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Modalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ModalidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ModalidadeService {

    private final IdHasher idHasher;
    private final ModalidadeRepository modalidadeRepository;

    // --- READ (Leitura) ---

    public Modalidade findById(Integer id) throws Exception {
        return modalidadeRepository.findById(id)
                .orElseThrow(() -> new Exception("Modalidade não encontrada com o ID: " + id));
    }

    public Modalidade findById(String hashedId) throws Exception {
        return findById(idHasher.decode(hashedId));
    }

    public ModalidadeDto findByIdDto(String hashedId) throws Exception {
        return converterParaDto(findById(hashedId));
    }

    public List<ModalidadeDto> findAll() {
        return modalidadeRepository.findAll().stream()
                .map(this::converterParaDto)
                .toList();
    }

    // --- CREATE (Criação) ---

    public ModalidadeDto create(ModalidadeDto dto) {
        Modalidade novaModalidade = new Modalidade();
        novaModalidade.setNome(dto.nome());
        Modalidade salva = modalidadeRepository.save(novaModalidade);
        return converterParaDto(salva);
    }

    // --- UPDATE (Atualização) ---

    public ModalidadeDto update(String hashedId, ModalidadeDto dto) throws Exception {
        // 1. Localizar a modalidade existente
        Modalidade modalidadeExistente = findById(hashedId);

        // 2. Atualizar os campos (neste caso, apenas o nome)
        modalidadeExistente.setNome(dto.nome());

        // 3. Guardar as alterações
        Modalidade atualizada = modalidadeRepository.save(modalidadeExistente);
        return converterParaDto(atualizada);
    }

    // --- DELETE (Remoção) ---

    public void delete(String hashedId) throws Exception {
        Integer idOriginal = idHasher.decode(hashedId);
        if (!modalidadeRepository.existsById(idOriginal)) {
            throw new Exception("Impossível remover: Modalidade não existe.");
        }
        modalidadeRepository.deleteById(idOriginal);
    }

    // --- HELPER (Conversão) ---

    public ModalidadeDto converterParaDto(Modalidade modalidade) {
        if (modalidade == null) return null;
        return new ModalidadeDto(
                idHasher.encode(modalidade.getId()),
                modalidade.getNome()
        );
    }
}