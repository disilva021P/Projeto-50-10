package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

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

    @Autowired
    public AulaService(AulaRepository aulaRepository,UtilizadoreRepository utilizadoreRepository) {
        this.aulaRepository = aulaRepository;
        this.utilizadoreRepository= utilizadoreRepository;
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
    public List<Aula> buscarAulaporEmail_Data(LocalDate dataAula, String email){
        Optional<Utilizadore> utilizador = utilizadoreRepository.findByEmail(email);
        if(utilizador.isEmpty()) throw new RuntimeException("Erro a encontrar utilizador");

        return aulaRepository.findByDataEAluno(dataAula,utilizador.get().getId());
    }
}