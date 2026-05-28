package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TipoPagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TipoPagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPagamentoService {

    private final TipoPagamentoRepository tipoPagamentoRepository;
    private final IdHasher idHasher;

    public List<TipoPagamentoDto> listarTodos() {
        return tipoPagamentoRepository.findAll()
                .stream()
                .map(t -> new TipoPagamentoDto(
                        idHasher.encode(t.getId()),
                        t.getTipoPagamento()
                ))
                .toList();
    }
}
