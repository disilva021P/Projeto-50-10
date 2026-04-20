package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DisponibilidadeProfessorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AulaCoaching;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.DisponibilidadeProfessor;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoAula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaProfessoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.DisponibilidadeProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DisponibilidadeService {
    private final DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;
    private final IdHasher idHasher;
    private final AulaProfessoreRepository aulaProfessoreRepository;
    private final ProfessorService professorService;
    public DisponibilidadeProfessor findById(Integer id) throws Exception{
        return disponibilidadeProfessorRepository.findById(id).orElseThrow(()-> new Exception("Disponibilidade não encontrada"));
    }
    public DisponibilidadeProfessor findById(String id) throws Exception{
        return disponibilidadeProfessorRepository.findById(idHasher.decode( id)).orElseThrow(()-> new Exception("Disponibilidade não encontrada"));
    }
    public List<DisponibilidadeProfessor> disponibilidadesByProfessorId(String id){
        return disponibilidadeProfessorRepository.findAllByProfessor_Id(idHasher.decode(id));
    }

    private boolean verificaDisponibilidade(String id, LocalDate data, LocalTime horaInicio, LocalTime horaFim){
        int diaSemana = data.getDayOfWeek().getValue();
        Optional<DisponibilidadeProfessor> resultado = disponibilidadeProfessorRepository.verificarDisponibilidade(idHasher.decode(id), diaSemana,horaInicio,horaFim );
        if(resultado.isEmpty()) return false;
        LocalDate inicio = resultado.get().getValidoDe();
        LocalDate fim = resultado.get().getValidoAte();
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }

    private boolean verificaProfessorJaPossuiAulas(String idProfessor, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        return aulaProfessoreRepository.professorJaPossuiAula(idHasher.decode(idProfessor),data,horaInicio,horaFim);
    }

    public boolean verificaMarcacaoValida(String idProfessor, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        return verificaDisponibilidade(idProfessor, data, horaInicio, horaFim)
                && !verificaProfessorJaPossuiAulas(idProfessor, data, horaInicio, horaFim);
    }

    // Corrigido: Agora usa a dataDesejada para verificar ambos os critérios
    public boolean verificaMarcacaoValida(DisponibilidadeProfessorDto dto, LocalDate dataDesejada) {
        String idProf = dto.professor().utilizadores().id();
        return verificaMarcacaoValida(idProf, dataDesejada, dto.horaInicio(), dto.horaFim());
    }

    // --- MÉTODOS CRUD ---

    public DisponibilidadeProfessorDto inserirDisponibilidade(DisponibilidadeProfessorDto dto) throws Exception {
        DisponibilidadeProfessor nova = new DisponibilidadeProfessor();

        // Mapeamento manual (ou usa ModelMapper/MapStruct)
        nova.setProfessor(professorService.findById(idHasher.decode(dto.professor().utilizadores().id())));
        nova.setDiaSemana(dto.diaSemana());
        nova.setHoraInicio(dto.horaInicio());
        nova.setHoraFim(dto.horaFim());
        nova.setValidoDe(dto.validoDe());
        nova.setValidoAte(dto.validoAte());

        DisponibilidadeProfessor salva = disponibilidadeProfessorRepository.save(nova);
        return converterParaDto(salva);
    }

    public DisponibilidadeProfessorDto alterarDisponibilidade(String id, DisponibilidadeProfessorDto dto) throws Exception {
        DisponibilidadeProfessor existente = findById(id);

        existente.setDiaSemana(dto.diaSemana());
        existente.setHoraInicio(dto.horaInicio());
        existente.setHoraFim(dto.horaFim());
        existente.setValidoDe(dto.validoDe());
        existente.setValidoAte(dto.validoAte());

        return converterParaDto(disponibilidadeProfessorRepository.save(existente));
    }

    public void removerDisponibilidade(String id) throws Exception {
        DisponibilidadeProfessor existente = findById(id);
        disponibilidadeProfessorRepository.delete(existente);
    }

    // --- AUXILIARES ---

    public DisponibilidadeProfessorDto converterParaDto(DisponibilidadeProfessor dp) {
        if (dp == null) return null;
        return new DisponibilidadeProfessorDto(
                idHasher.encode(dp.getId()),
                professorService.convertToDto(dp.getProfessor()),
                dp.getDiaSemana(),
                dp.getHoraInicio(),
                dp.getHoraFim(), // Corrigido aqui (estava horaInicio duas vezes)
                dp.getValidoDe(),
                dp.getValidoAte()
        );
    }

}
