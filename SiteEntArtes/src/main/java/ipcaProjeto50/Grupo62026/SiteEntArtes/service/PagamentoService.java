package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final IdHasher idHasher;

    @Autowired
    public PagamentoService(PagamentoRepository pagamentoRepository,IdHasher idHasher) {
        this.pagamentoRepository = pagamentoRepository;
        this.idHasher=idHasher;
    }

    // Listar todos os pagamentos ,
    public List<PagamentoDto> listarTodos(){return pagamentoRepository.findAll().stream().map(this::converterParaDto).toList();}

    // Procurar uma pagamento por ID
    public Optional<PagamentoDto> buscarPorId(String id) {
        return pagamentoRepository.findById(idHasher.decode(id)).map(this::converterParaDto);
    }
    // Criar pagamento
    public PagamentoDto criar(PagamentoDto dto) {
        //  Criamos uma Entity vazia
        Pagamento entidade = new Pagamento();

        //  Passamos os dados do DTO (que veio do JS) para a Entity
        entidade.setValorPagamento(dto.valorPagamento());
        entidade.setDescricao(dto.descricao());
        entidade.setPago(false); // Por defeito, ninguém começa com a conta paga
        entidade.setDataPagamento(LocalDate.now());
        entidade.setIdTipoPagamento(dto.idTipoPagamento());
        entidade.setAula(dto.aula());

        //  Mandamos o Repository gravar a Entity na BD
        Pagamento gravado = pagamentoRepository.save(entidade);

        //  Transformamos de volta em DTO para responder ao JS
        return converterParaDto(gravado);
    }

    // Atualizar pagamento VERIFICAR MELHOR DEPOIS
    public Pagamento atualizar(Integer id, Pagamento pagamentoAtualizado) {

        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        pagamento.setValorPagamento(pagamentoAtualizado.getValorPagamento());
        pagamento.setDescricao(pagamentoAtualizado.getDescricao());
        pagamento.setDataPagamento(pagamentoAtualizado.getDataPagamento());

        return pagamentoRepository.save(pagamento);
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
    // Agora o JS pode mandar o ID seguro ("abc123")
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
                null // Aqui podes depois criar um UtilizadorResumoDto se precisares
        );
    }
    @Scheduled(cron = "0 0 0 1 * *")
    public void tarefaInicioDoMes() {
        System.out.println("Executando no primeiro dia do mês às 00:00");
    }

}

