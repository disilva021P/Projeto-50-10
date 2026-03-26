package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AulaService {
    private final UtilizadoreRepository utilizadoreRepository;
    private final AulaRepository aulaRepository;
    private final IdHasher idHasher;
    @Autowired
    public AulaService(AulaRepository aulaRepository,UtilizadoreRepository utilizadoreRepository, IdHasher idHasher) {
        this.aulaRepository = aulaRepository;
        this.utilizadoreRepository= utilizadoreRepository;
        this.idHasher = idHasher;
    }

    // Listar todas as aulas
    public List<Aula> findAll() {
        return aulaRepository.findAll();
    }

    public PagedModel<Aula> findAll(Pageable paginacao) {
        Page<Aula> page = aulaRepository.findAll(paginacao);
        return new PagedModel<>(page);
    }

    // Procurar uma aula por ID
    public Optional<Aula> buscarPorId(Integer id) {
        return aulaRepository.findById(id);
    }

    // Criar ou Atualizar uma aula
    public Aula salvar(Aula aula) {
        return aulaRepository.save(aula);
    }

    // Eliminar uma aula
    public void eliminar(Integer id) {
        aulaRepository.deleteById(id);
    }

    public List<Aula> findByDataAula(LocalDate data) {
        LocalDate dia = LocalDate.now();
        return aulaRepository.findByDataAula(dia);
    }
    public List<AulaDto> buscarAulaporEmail_Data(LocalDate dataAula, String email){
        Optional<Utilizadore> utilizador = utilizadoreRepository.findByEmail(email);
        if(utilizador.isEmpty()) throw new RuntimeException("Erro a encontrar utilizador");
        List<Aula> aulas = aulaRepository.findByDataEAluno(dataAula,utilizador.get().getId());
        return converterListaAulaParaAulaDto(aulas);
    }

    public List<AulaDto> converterListaAulaParaAulaDto(List<Aula> aulas) {
         return aulas.stream()
                .map(this::converterParaDto)
                .toList();
    }
    public AulaDto converterParaDto(Aula aula) {
        return new AulaDto(
                idHasher.encode(aula.getId()),
                aula.getModalidade(),
                aula.getEstudio(),
                aula.getMaxAlunos(),
                aula.getDuracaoMinutos(),
                aula.getDataAula(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getEstado(),
                aula.getTipoAula()
        );
    }

}