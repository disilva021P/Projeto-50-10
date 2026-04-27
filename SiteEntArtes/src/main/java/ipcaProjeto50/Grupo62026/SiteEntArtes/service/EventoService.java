package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.CriarEventosDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EventoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstadoAulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EventoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ParticipantesEventoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final ParticipantesEventoRepository participantesEventoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;
    private final EstadoAulaRepository estadoAulaRepository;


    private EventoDto toDto(Evento evento) {
        return new EventoDto(
                idHasher.encode(evento.getId()),
                evento.getNome(),
                evento.getDescricao(),
                evento.getDataEvento(),
                evento.getHoraInicio(),
                evento.getHoraFim(),
                evento.getLocal(),
                new UtilizadoreResumoDto(idHasher.encode(evento.getCriadoPor().getId()), evento.getCriadoPor().getNome())
        );
    }

    public PagedModel<EventoDto> findAll(Pageable pageable) {
        return new PagedModel<>(eventoRepository.findAll(pageable).map(this::toDto));
    }

    public EventoDto findById(String idHashed) throws Exception {
        Evento evento = eventoRepository.findById(idHasher.decode(idHashed))
                .orElseThrow(() -> new Exception("Evento não encontrado"));
        return toDto(evento);
    }

    public List<EventoDto> findEventosFuturos() {
        return eventoRepository
                .findByDataEventoAfterOrderByDataEventoAsc(LocalDate.now())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<EventoDto> findEventosPorUtilizador(String utilizadorIdHashed) {
        Integer uId = idHasher.decode(utilizadorIdHashed);
        return participantesEventoRepository.findEventosAtivosPorUtilizador(uId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public EventoDto criarEvento(String criadorIdHashed, CriarEventosDto dto) throws Exception {
        Utilizadore criador = utilizadoreRepository.findById(idHasher.decode(criadorIdHashed))
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        Evento evento = new Evento();
        evento.setNome(dto.nome());
        evento.setDescricao(dto.descricao());
        evento.setDataEvento(dto.dataEvento());
        evento.setLocal(dto.local());
        evento.setCriadoPor(criador);
        Evento saved = eventoRepository.save(evento);

        // Adiciona participantes se existirem
        if (dto.participantesIds() != null) {
            for (String participanteIdHashed : dto.participantesIds()) {
                Utilizadore participante = utilizadoreRepository
                        .findById(idHasher.decode(participanteIdHashed))
                        .orElseThrow(() -> new Exception("Participante não encontrado: " + participanteIdHashed));

                ParticipantesEvento pe = new ParticipantesEvento();
                pe.setEvento(saved);
                pe.setUtilizador(participante);
                pe.setPago(false);
                pe.setCancelado(false);
                participantesEventoRepository.save(pe);
            }
        }

        return toDto(saved);
    }

    @Transactional
    public EventoDto update(String idHashed, CriarEventosDto dto) throws Exception {
        Evento evento = eventoRepository.findById(idHasher.decode(idHashed))
                .orElseThrow(() -> new Exception("Evento não encontrado"));

        evento.setNome(dto.nome());
        evento.setDescricao(dto.descricao());
        evento.setDataEvento(dto.dataEvento());
        evento.setLocal(dto.local());

        return toDto(eventoRepository.save(evento));
    }

    @Transactional
    public void delete(String idHashed) throws Exception {
        Integer id = idHasher.decode(idHashed);
        if (!eventoRepository.existsById(id)) {
            throw new Exception("Evento não encontrado");
        }
        eventoRepository.deleteById(id);
    }

    @Transactional
    public void adicionarParticipante(String eventoIdHashed, String utilizadorIdHashed) throws Exception {
        Integer eventoId = idHasher.decode(eventoIdHashed);
        Integer utilizadorId = idHasher.decode(utilizadorIdHashed);

        if (participantesEventoRepository.existsByEventoIdAndUtilizadorId(eventoId, utilizadorId)) {
            throw new Exception("Utilizador já é participante deste evento");
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new Exception("Evento não encontrado"));
        Utilizadore utilizador = utilizadoreRepository.findById(utilizadorId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        ParticipantesEvento pe = new ParticipantesEvento();
        pe.setEvento(evento);
        pe.setUtilizador(utilizador);
        pe.setPago(false);
        pe.setCancelado(false);
        participantesEventoRepository.save(pe);
    }

    @Transactional
    public void removerParticipante(String eventoIdHashed, String utilizadorIdHashed) throws Exception {
        Integer eventoId = idHasher.decode(eventoIdHashed);
        Integer utilizadorId = idHasher.decode(utilizadorIdHashed);

        if (!participantesEventoRepository.existsByEventoIdAndUtilizadorId(eventoId, utilizadorId)) {
            throw new Exception("Utilizador não é participante deste evento");
        }

        participantesEventoRepository.deleteByEventoIdAndUtilizadorId(eventoId, utilizadorId);
    }

    @Transactional
    public void cancelarInscricao(String eventoIdHashed, String utilizadorIdHashed) throws Exception {
        ParticipantesEventoId idComposto = new ParticipantesEventoId(
                idHasher.decode(eventoIdHashed),
                idHasher.decode(utilizadorIdHashed)
        );
        ParticipantesEvento pe = participantesEventoRepository.findById(idComposto)
                .orElseThrow(() -> new Exception("Inscrição não encontrada"));
        pe.setCancelado(true);
        participantesEventoRepository.save(pe);
    }

    @Transactional
    public void editarEstado(String idHashed, Integer estadoId) throws Exception {
        Evento evento = eventoRepository.findById(idHasher.decode(idHashed))
                .orElseThrow(() -> new Exception("Evento não encontrado"));
        EstadoAula novoEstado = estadoAulaRepository.findById(estadoId)
                .orElseThrow(() -> new Exception("Estado inválido"));
        evento.setEstadoAula(novoEstado);
        eventoRepository.save(evento);
    }

    @Transactional
    public void inscreverParticipante(String eventoHashId, String utilizadorHashId) throws Exception {
        Integer eventoId = idHasher.decode(eventoHashId);
        Integer utilizadorId = idHasher.decode(utilizadorHashId);

        ParticipantesEventoId idComposto = new ParticipantesEventoId(eventoId, utilizadorId);

        Optional<ParticipantesEvento> inscricaoExistente = participantesEventoRepository.findById(idComposto);

        if (inscricaoExistente.isPresent()) {
            ParticipantesEvento pe = inscricaoExistente.get();
            if (pe.getCancelado()) {
                pe.setCancelado(false);
                participantesEventoRepository.save(pe);
                return;
            } else {
                throw new Exception("Utilizador já está inscrito e ativo neste evento.");
            }
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new Exception("Evento não encontrado"));
        Utilizadore utilizador = utilizadoreRepository.findById(utilizadorId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        ParticipantesEvento inscricao = new ParticipantesEvento();
        inscricao.setId(idComposto);
        inscricao.setEvento(evento);
        inscricao.setUtilizador(utilizador);
        inscricao.setPago(false);
        inscricao.setCancelado(false);

        participantesEventoRepository.save(inscricao);
    }
}