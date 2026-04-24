package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessoreDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ProfessorModalidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Professore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ProfessorModalidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ProfessoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfessorService {
    private final IdHasher idHasher;
    private final ProfessoreRepository professoreRepository;
    private final ProfessorModalidadeRepository professorModalidadeRepository;
    public Professore findById(Integer id) throws Exception {
        return professoreRepository.findById(id).orElseThrow(()-> new Exception("Professor não encontrado"));
    }
    public Professore findById(String id) throws Exception {
        return professoreRepository.findById(idHasher.decode(id)).orElseThrow(()-> new Exception("Professor não encontrado"));
    }
    public List<ProfessoreDto> findAll(){
        return professoreRepository.findAll().stream().map(this::convertToDto).toList();
    }
    // No ProfessorService.java

    public Page<ProfessoreDto> findAllPageable(Pageable pageable) {
        return professoreRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Page<ProfessoreDto> findByModalidade(String modalidadeId, Pageable pageable) {
        Integer idReal = idHasher.decode(modalidadeId);

        // 1. Obtemos a página de ProfessorModalidade do repositório
        Page<ProfessorModalidade> pmPage = professorModalidadeRepository.findByModalidade_Id(idReal, pageable);

        // 2. Usamos o .map() do próprio Page para transformar o conteúdo
        // Isso mantém o total de elementos, páginas, etc., mas troca o conteúdo para DTO do Professor
        return pmPage.map(pm -> convertToDto(pm.getProfessor()));
    }
    public ProfessoreDto convertToDto(Professore p){
        return p == null ? null : new ProfessoreDto(
                new UtilizadoreResumoDto(idHasher.encode(p.getId()),p.getUtilizadores().getNome() ),
                p.getValorHora(),
                p.getProfessorExterno()
        );
    }
}
