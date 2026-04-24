package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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
    private final TipoUtilizadorRepository tipoUtilizadorRepository;
    private final TipoPagamentoRepository tipoPagamentoRepository;
    private final AulaService aulaService;

    // Listar todos os pagamentos ,
    public List<PagamentoDto> listarTodos() {
        return pagamentoRepository.findAll().stream().map(this::converterParaDto).toList();
    }

    // Procurar uma pagamento por ID
    public Optional<PagamentoDto> buscarPorId(String id) {
        return pagamentoRepository.findById(idHasher.decode(id)).map(this::converterParaDto);
    }

    // Criar pagamento
    public PagamentoDto criar(PagamentoDto dto) throws Exception {

        //  Criamos uma Entity vazia
        Pagamento entidade = new Pagamento();

        String idHashed = dto.utilizadoreResumoDto().id();
        Integer idReal = idHasher.decode(idHashed);

        String idHashed2 = dto.idTipoPagamento();
        Integer idReal2= idHasher.decode(idHashed2);

        Utilizadore donoDoPagamento = utilizadoreRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));
        TipoPagamento tipoPagamento= tipoPagamentoRepository.findById(idReal2)
                .orElseThrow(() -> new RuntimeException("Tipo nao encontrado"));
        Aula aula = aulaService.bucarPorId(dto.id());
        //  Passamos os dados do DTO (que veio do JS) para a Entity
        entidade.setValorPagamento(dto.valorPagamento());
        entidade.setDescricao(dto.descricao());
        entidade.setPago(false); // Por defeito, ninguém começa com a conta paga
        entidade.setDataPagamento(LocalDate.now());
        entidade.setIdTipoPagamento(tipoPagamento);
        entidade.setAula(aula);

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

        TipoPagamento tipoPagamento= tipoPagamentoRepository.findById(idHasher.decode(dto.idTipoPagamento()))
                .orElseThrow(() -> new RuntimeException("Tipo nao encontrado"));

        pagamento.setValorPagamento(dto.valorPagamento());
        pagamento.setDescricao(dto.descricao());
        pagamento.setDataPagamento(dto.dataPagamento());
        pagamento.setIdTipoPagamento(tipoPagamento);

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
                idHasher.encode(pagamento.getIdTipoPagamento().getId()), // Objeto completo
                nomeTipo,                       // Apenas o nome (String)
               aulaService.converterParaDto(pagamento.getAula()),
                pagamento.getDataPagamento(),
                pagamento.getDataConfirmado(),
                resumo // associa o utilizador ao pagamento
        );
    }

    // Listar pagamentos de um utilizador específico filtrado por Mês/Ano (via offset)
    public List<PagamentoDto> listarPorUtilizador(String utilizadorIdHashed, Integer offset) {
        // 1. Descodifica o ID e calcula a data alvo
        Integer idReal = idHasher.decode(utilizadorIdHashed);
        LocalDate dataAlvo = LocalDate.now().plusMonths(offset);

        // 2. Procura na BD usando o filtro de Mês e Ano
        return pagamentoRepository.findAllByUtilizadorAndMesEAno(
                        idReal,
                        dataAlvo.getMonthValue(),
                        dataAlvo.getYear()
                )
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    public PagamentosEstatisiticaCoordenacao EstatisticasCoordenacao() {
        return pagamentoRepository.getEstatisticas(List.of(1, 2,3,4));
    }

    public DespesasEstatisticaDto DespesasEstatistica() {
        return pagamentoRepository.getEstatisticasDespesas(List.of(5, 6,7));
    }

    public ProfessorEstatisticaDto EstatisticaProfessor(String idHashed, Integer offset) {
        Integer idReal = idHasher.decode(idHashed);
        LocalDate dataAlvo = LocalDate.now().plusMonths(offset); // Calcula a data com base no offset

        // Precisarias de uma nova query no Repository que aceite mes e ano
        return pagamentoRepository.getEstatisticasProfessor(
                idReal,
                dataAlvo.getMonthValue(),
                dataAlvo.getYear()
        );
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
    public AlunoEstatisiticaDto obterEstatisticasAluno(String idHashed, Integer offset) {
        Integer idReal = idHasher.decode(idHashed);
        LocalDate dataAlvo = LocalDate.now().plusMonths(offset);

        // 1. Totais filtrados pelo mês/ano do offset
        BigDecimal totalPago = pagamentoRepository.somarPagoPorUtilizador(
                idReal,
                dataAlvo.getMonthValue(),
                dataAlvo.getYear()
        );
        BigDecimal totalPendente = pagamentoRepository.somarPendentePorUtilizador(
                idReal,
                dataAlvo.getMonthValue(),
                dataAlvo.getYear()
        );
        // 2. Histórico TAMBÉM filtrado pelo mês/ano do offset
        List<PagamentoDto> historico = pagamentoRepository.findAllByUtilizadorAndMesEAno(
                        idReal,
                        dataAlvo.getMonthValue(),
                        dataAlvo.getYear()
                )
                .stream()
                .map(this::converterParaDto)
                .toList();

        return new AlunoEstatisiticaDto(totalPago, totalPendente, historico);
    }

    public PagedModel<PagamentoDto> findAllPorUtilizador(String idHashed, Pageable paginacao) {
        // 1. Descodifica o ID
        Integer idReal = idHasher.decode(idHashed);

        // 2. Procura na BD com paginação e mapeia para DTO
        Page<PagamentoDto> page = pagamentoRepository
                .findAllByIdutilizador_Id(idReal, paginacao)
                .map(this::converterParaDto);

        // 3. Retorna o modelo paginado
        return new PagedModel<>(page);
    }
}

