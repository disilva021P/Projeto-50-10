package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;

    @Autowired
    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    // Listar todas as aulas
    public List<Aula> listarTodas() {
        return aulaRepository.findAll();
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
}