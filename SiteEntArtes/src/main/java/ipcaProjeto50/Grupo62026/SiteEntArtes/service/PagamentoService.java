package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAluno;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EncarregadoAlunoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.PagamentoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final IdHasher idHasher;
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;
    private final UtilizadoreRepository utilizadoreRepository;

    // Listar todos os pagamentos ,
    public List<PagamentoDto> listarTodos() {
        return pagamentoRepository.findAll().stream().map(this::converterParaDto).toList();
    }

    // Procurar uma pagamento por ID
    public Optional<PagamentoDto> buscarPorId(String id) {
        return pagamentoRepository.findById(idHasher.decode(id)).map(this::converterParaDto);
    }

    // Criar pagamento
    public PagamentoDto criar(PagamentoDto dto) {

        //  Criamos uma Entity vazia
        Pagamento entidade = new Pagamento();

        String idHashed = dto.utilizadoreResumoDto().id();
        Integer idReal = idHasher.decode(idHashed);

        Utilizadore donoDoPagamento = utilizadoreRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));

        //  Passamos os dados do DTO (que veio do JS) para a Entity
        entidade.setValorPagamento(dto.valorPagamento());
        entidade.setDescricao(dto.descricao());
        entidade.setPago(false); // Por defeito, ninguém começa com a conta paga
        entidade.setDataPagamento(LocalDate.now());
        entidade.setIdTipoPagamento(dto.idTipoPagamento());
        entidade.setAula(dto.aula());

        entidade.setIdutilizador(donoDoPagamento);
        //  Mandamos o Repository gravar a Entity na BD
        Pagamento gravado = pagamentoRepository.save(entidade);

        //  Transformamos de volta em DTO para responder ao JS
        return converterParaDto(gravado);
    }

    // Atualizar pagamento
    public PagamentoDto atualizar(String idHashed, PagamentoDto dto) {

        Integer idReal = idHasher.decode(idHashed);

        Pagamento pagamento = pagamentoRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        pagamento.setValorPagamento(dto.valorPagamento());
        pagamento.setDescricao(dto.descricao());
        pagamento.setDataPagamento(dto.dataPagamento());

        if (dto.idTipoPagamento() != null) {
            pagamento.setIdTipoPagamento(dto.idTipoPagamento());
        }

        // 4. Se precisares de mudar o dono do pagamento (Utilizador)
        if (dto.utilizadoreResumoDto() != null) {
            Integer novoUserId = idHasher.decode(dto.utilizadoreResumoDto().id());
            Utilizadore novoDono = utilizadoreRepository.findById(novoUserId)
                    .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
            pagamento.setIdutilizador(novoDono);
        }
        Pagamento gravado = pagamentoRepository.save(pagamento);
        return converterParaDto(gravado);
    }

    // Confirmar pagamento
    public PagamentoDto confirmar(String idHashed) {

        //  Usamos o hasher para saber qual é o ID real (Integer)
        Integer idReal = idHasher.decode(idHashed);

        //  Vamos buscar à base de dados
        Pagamento pagamento = pagamentoRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        //  Fazemos a alteração (o "tempero" do cozinheiro)
        pagamento.setPago(true);
        pagamento.setDataConfirmado(LocalDate.now());

        //  Guardamos a Entity alterada
        Pagamento pagamentoGuardado = pagamentoRepository.save(pagamento);

        //  TRANSFORMAMOS a Entity em DTO antes de enviar para o Controller
        return converterParaDto(pagamentoGuardado);
    }

    //  Eliminar um pagamento   
    public void eliminar(String idHashed) {
        //  Descodificamos para o número real da BD
        Integer idReal = idHasher.decode(idHashed);

        //  Apagamos o registo
        pagamentoRepository.deleteById(idReal);
    }

    // Converter lista de Pagamentos para PagamentosDTO
    public List<PagamentoDto> converterListaPagamentoParaDto(List<Pagamento> pagamentos) {
        return pagamentos.stream()
                .map(this::converterParaDto)
                .toList();
    }

    //converter de pagamento para dto
    public PagamentoDto converterParaDto(Pagamento pagamento) {
        // Verificamos se o tipo de pagamento existe antes de pedir o nome
        String nomeTipo = (pagamento.getIdTipoPagamento() != null)
                ? pagamento.getIdTipoPagamento().getTipoPagamento()
                : "Não definido";

        UtilizadoreResumoDto resumo = null;
        if (pagamento.getIdutilizador() != null) {
            resumo = new UtilizadoreResumoDto(idHasher.encode(pagamento.getIdutilizador().getId()),
                    pagamento.getIdutilizador().getNome());
        }

        return new PagamentoDto(
                idHasher.encode(pagamento.getId()), // ID seguro para o JS
                pagamento.getValorPagamento(),
                pagamento.getPago(),
                pagamento.getDescricao(),
                pagamento.getIdTipoPagamento(), // Objeto completo
                nomeTipo,                       // Apenas o nome (String)
                pagamento.getAula(),
                pagamento.getDataPagamento(),
                pagamento.getDataConfirmado(),
                resumo // associa o utilizador ao pagamento
        );
    }

    // encontrar os filhos dos encarregados de educacao
    public List<UtilizadoreResumoDto> findEducandosdeEducador(Integer idEducador) {
        return encarregadoAlunoRepository.findAllByEncarregado_Id(idEducador).stream()
                .map(ea -> new UtilizadoreResumoDto(
                        idHasher.encode(ea.getAluno().getId()),
                        ea.getAluno().getUtilizadores().getNome()
                ))
                .toList();
    }


    public PagamentosEstatisiticaCoordenacao EstatisticasCoordenacao() {
        return pagamentoRepository.getEstatisticas();
    }

    public DespesasEstatisticaDto DespesasEstatistica() {
        return pagamentoRepository.getEstatisticasDespesas(List.of(5, 6));
    }

    public ProfessorEstatisticaDto EstatisticaProfessor(String idHashed) {
        Integer idReal = idHasher.decode(idHashed);

        return pagamentoRepository.getEstatisticasProfessor(idReal);
    }

    public String escreverPagamentosCsv( List<PagamentoDto> pagamentos) {
        StringBuilder sb = new StringBuilder();

        sb.append("sep=;\n");
        sb.append("Utilizador;Descricao;Valor;Data;Estado");

        for (PagamentoDto p : pagamentos) {
            String nome = (p.utilizadoreResumoDto() != null) ? p.utilizadoreResumoDto().nome() : "N/A";
            sb.append(nome).append(";");
            sb.append(p.descricao()).append(";");
            sb.append(p.valorPagamento()).append(";");
            sb.append(p.dataPagamento()).append(";");
            sb.append(p.pago() ? "Pago" : "Pendente").append("\n");
        }
        return sb.toString();
    }

    public String exportarRelatorioMensalTexto(int mes, int ano) {
        List<Pagamento> pagamentos = pagamentoRepository.findByMesEAno(mes, ano);

        List<PagamentoDto> dtos = pagamentos.stream()
                .map(this::converterParaDto)
                .toList();

        return escreverPagamentosCsv(dtos);
    }
    public AlunoEstatisiticaDto obterEstatisticasAluno(String idHashed) {
        Integer idReal = idHasher.decode(idHashed);

        // Chamamos as queries que criamos acima
        BigDecimal totalPago = pagamentoRepository.somarPagoPorUtilizador(idReal);
        BigDecimal totalPendente = pagamentoRepository.somarPendentePorUtilizador(idReal);

        // Histórico convertido para DTO
        List<PagamentoDto> historico = pagamentoRepository.findAllByIdutilizador_Id(idReal)
                .stream()
                .map(this::converterParaDto)
                .toList();

        return new AlunoEstatisiticaDto(totalPago,totalPendente,historico);
    }
}


