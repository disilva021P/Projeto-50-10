package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import io.jsonwebtoken.ExpiredJwtException;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final AulaRepository aulaRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    // Listar todos os pagamentos ,
    public List<PagamentoDto> listarTodos() {
        return pagamentoRepository.findAll().stream().map(this::converterParaDto).toList();
    }

    // Procurar um pagamento por ID
    public Optional<PagamentoDto> buscarPorId(String id) {
        return pagamentoRepository.findById(idHasher.decode(id)).map(this::converterParaDto);
    }

    // Criar pagamento
    @Transactional
    public PagamentoDto criar(CriarPagamentoDto dto) throws Exception { // <--- Alterado aqui

        if(dto.dataPagamento() != null && dto.dataPagamento().isBefore(LocalDate.now())){
            throw new Exception("Só pode marcar pagamentos futuros");
        }
        if (dto.valorPagamento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Valor não pode ser 0 ou menor que 0");
        }

        // Criamos uma Entity vazia
        Pagamento entidade = new Pagamento();

        // No CriarPagamentoDto o ID do utilizador vem direto na raiz
        String idHashed = dto.idUtilizador();
        Integer idReal = idHasher.decode(idHashed);

        String idHashed2 = dto.idTipoPagamento();
        Integer idReal2 = idHasher.decode(idHashed2);

        Utilizadore donoDoPagamento = utilizadoreRepository.findById(idReal)
                .orElseThrow(() -> new Exception("Utilizador nao encontrado"));
        TipoPagamento tipoPagamento = tipoPagamentoRepository.findById(idReal2)
                .orElseThrow(() -> new Exception("Tipo nao encontrado"));

        Aula aula = null;
        if (dto.idAula() != null && !dto.idAula().isBlank()) {
            aula = aulaRepository.findById(idHasher.decode(dto.idAula()))
                    .orElseThrow(() -> new Exception("Aula não encontrada"));
        }

        entidade.setValorPagamento(dto.valorPagamento());
        entidade.setDescricao(dto.descricao());
        entidade.setDataPagamento(dto.dataPagamento() != null ? dto.dataPagamento() : LocalDate.now());
        entidade.setIdTipoPagamento(tipoPagamento);
        entidade.setAula(aula);
        entidade.setIdutilizador(donoDoPagamento);

        // Se for tipo "Pagamento" (a professores), fica automaticamente liquidado
        boolean isPagamentoProfessor = tipoPagamento.getTipoPagamento().equalsIgnoreCase("Pagamento");
        entidade.setPago(isPagamentoProfessor);
        entidade.setDataConfirmado(isPagamentoProfessor ? LocalDate.now() : null);

        // Mandamos o Repository gravar a Entity na BD
        Pagamento gravado = pagamentoRepository.save(entidade);

        // Transformamos de volta em DTO completo para responder ao Controller
        return converterParaDto(gravado);
    }

    // Atualizar pagamento
    @Transactional
    public PagamentoDto atualizar(String idHashed, PagamentoDto dto) throws Exception {

        Integer idReal = idHasher.decode(idHashed);

        Pagamento pagamento = pagamentoRepository.findById(idReal)
                .orElseThrow(() -> new Exception("Pagamento não encontrado"));

        TipoPagamento tipoPagamento= tipoPagamentoRepository.findById(idHasher.decode(dto.idTipoPagamento()))
                .orElseThrow(() -> new Exception("Tipo nao encontrado"));

        pagamento.setValorPagamento(dto.valorPagamento());
        pagamento.setDescricao(dto.descricao());
        pagamento.setDataPagamento(dto.dataPagamento() != null ? dto.dataPagamento() : LocalDate.now());
        pagamento.setIdTipoPagamento(tipoPagamento);

        // 4. Se precisares de mudar o dono do pagamento (Utilizador)
        if (dto.utilizadoreResumoDto() != null) {
            Integer novoUserId = idHasher.decode(dto.utilizadoreResumoDto().id());
            Utilizadore novoDono = utilizadoreRepository.findById(novoUserId)
                    .orElseThrow(() -> new Exception("Utilizador não encontrado"));
            pagamento.setIdutilizador(novoDono);
        }
        Pagamento gravado = pagamentoRepository.save(pagamento);
        return converterParaDto(gravado);
    }

    // Confirmar pagamento
    @Transactional
    public PagamentoDto confirmar(String idHashed) throws Exception {

        //  Usamos o hasher para saber qual é o ID real (Integer)
        Integer idReal = idHasher.decode(idHashed);

        //  Vamos buscar à base de dados
        Pagamento pagamento = pagamentoRepository.findById(idReal)
                .orElseThrow(() -> new Exception("Pagamento não encontrado"));

        if (pagamento.getPago()) {
            throw new Exception("O Pagamento já se encontra liquidado.");
        }

        //  Fazemos a alteração (o "tempero" do cozinheiro)
        pagamento.setPago(true);
        pagamento.setDataConfirmado(LocalDate.now());

        //  Guardamos a Entity alterada
        Pagamento pagamentoGuardado = pagamentoRepository.save(pagamento);

        // ==========================================================================================
        // AUTOMAÇÃO FINANCEIRA: Divisão de Honorários (Professor + Retenção Coordenação)
        // ==========================================================================================
        try {
            // Verificamos se o pagamento liquidado é do tipo "Aula Avulso" (ID=2) e se tem uma aula associada
            if (pagamentoGuardado.getIdTipoPagamento() != null &&
                    pagamentoGuardado.getIdTipoPagamento().getId() == 2 &&
                    pagamentoGuardado.getAula() != null) {

                Integer aulaId = pagamentoGuardado.getAula().getId();

                // 1. Procurar o professor associado a esta aula através da tabela aula_professores
                Utilizadore professorDocente = null;
                try {
                    professorDocente = entityManager.createQuery(
                                    "SELECT ap.professor FROM AulaProfessore ap WHERE ap.aula.id = :aulaId", Utilizadore.class)
                            .setParameter("aulaId", aulaId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);
                } catch (Exception e) {
                    System.err.println("Aviso: Não foi possível localizar o professor associado à aula " + aulaId);
                }

                // 2. BUSCAR A TAXA DE RETENÇÃO DA ESCOLA (Ex: '20' para 20%)
                String taxaConfig = "20"; // Fallback se não estiver na BD
                try {
                    taxaConfig = entityManager.createQuery(
                                    "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :nomeConfig", String.class)
                            .setParameter("nomeConfig", "taxa_coaching_escola")
                            .getSingleResult();
                } catch (jakarta.persistence.NoResultException e) {
                    // Mantém os 20% por defeito
                }

                java.math.BigDecimal percentagemEscola = new java.math.BigDecimal(taxaConfig.trim());
                java.math.BigDecimal cemPercento = new java.math.BigDecimal("100");

                // Calcular frações
                java.math.BigDecimal fatorEscola = percentagemEscola.divide(cemPercento, 4, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal fatorProfessor = cemPercento.subtract(percentagemEscola).divide(cemPercento, 4, java.math.RoundingMode.HALF_UP);

                // Valores Absolutos (Ex: Aluno pagou 144.00€)
                java.math.BigDecimal valorTotalAluno = pagamentoGuardado.getValorPagamento();
                java.math.BigDecimal valorLucroEscola = valorTotalAluno.multiply(fatorEscola).setScale(2, java.math.RoundingMode.HALF_UP);       // 144 * 0.20 = 28.80€
                java.math.BigDecimal valorRemuneracaoProfessor = valorTotalAluno.multiply(fatorProfessor).setScale(2, java.math.RoundingMode.HALF_UP); // 144 * 0.80 = 115.20€

                // 3. Procurar os Tipos de Pagamento necessários (ID 7 para Professor, ID 2 ou outro para Receita da Escola)
                TipoPagamento tipoPagamentoProfessor = entityManager.find(TipoPagamento.class, 7);
                if (tipoPagamentoProfessor == null) {
                    throw new Exception("Tipo de pagamento com ID 7 não encontrado na base de dados.");
                }

                // ─── PARTE A: GRAVAR CRÉDITO DO PROFESSOR (Se ele existir) ───
                if (professorDocente != null) {
                    Pagamento creditoProfessor = new Pagamento();
                    creditoProfessor.setValorPagamento(valorRemuneracaoProfessor);
                    creditoProfessor.setPago(true);
                    creditoProfessor.setDataConfirmado(LocalDate.now());
                    creditoProfessor.setDescricao(String.format(
                            "Honorários Recebidos: Sessão Coaching de Aluno %s (Valor aluno: %.2f€ | Retenção Escola: %s%%)",
                            pagamentoGuardado.getIdutilizador().getNome(), valorTotalAluno, taxaConfig));
                    creditoProfessor.setIdutilizador(professorDocente);
                    creditoProfessor.setIdTipoPagamento(tipoPagamentoProfessor);
                    creditoProfessor.setDataPagamento(LocalDate.now());
                    creditoProfessor.setAula(pagamentoGuardado.getAula());

                    entityManager.persist(creditoProfessor);
                }

                // ─── PARTE B: GRAVAR MARGEM/LUCRO DA COORDENAÇÃO (ID = 1) ───
                // Buscamos o utilizador da Coordenação (ID 1) da base de dados
                Utilizadore coordenacao = entityManager.find(Utilizadore.class, 1);
                if (coordenacao != null) {
                    Pagamento receitaEscola = new Pagamento();
                    receitaEscola.setValorPagamento(valorLucroEscola); // Guarda os 28.80€
                    receitaEscola.setPago(true); // Fica imediatamente como liquidado
                    receitaEscola.setDataConfirmado(LocalDate.now());
                    receitaEscola.setDescricao(String.format(
                            "Margem de Retenção Escola: Sessão Coaching de Aluno %s (Valor total: %.2f€ | Retido: %s%%)",
                            pagamentoGuardado.getIdutilizador().getNome(), valorTotalAluno, taxaConfig));

                    receitaEscola.setIdutilizador(coordenacao); // Associa à conta da Coordenação (ID 1)
                    receitaEscola.setIdTipoPagamento(pagamentoGuardado.getIdTipoPagamento()); // Mantém como Tipo ID 2 (Aula Avulso) ou o que preferires
                    receitaEscola.setDataPagamento(LocalDate.now());
                    receitaEscola.setAula(pagamentoGuardado.getAula());

                    entityManager.persist(receitaEscola);
                }

                entityManager.flush();
            }
        } catch (Exception e) {
            System.err.println("Erro crítico na divisão automática de honorários: " + e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar divisão de honorários com a coordenação: " + e.getMessage());
        }
        // ==========================================================================================

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
        Aula aula=pagamento.getAula();
        if(aula!=null){
            // 1. Criar os DTOs de suporte (se necessário)
// Se o pagamento não precisar dos detalhes do estúdio ou estado, podes passar null
            EstudioDto estudioDto = (aula.getEstudio() != null) ?
                    new EstudioDto(idHasher.encode(aula.getEstudio().getId()), aula.getEstudio().getNome(),aula.getEstudio().getCapacidade(), aula.getEstudio().getNotas()) : null;

            EstadoAulaDto estadoDto = (aula.getEstado() != null) ?
                    new EstadoAulaDto(idHasher.encode(aula.getEstado().getId()), aula.getEstado().getEstado()) : null;

// 2. Instanciar o AulaDto usando o construtor que forneceste
            AulaDto aulaDtoManual = new AulaDto(
                    idHasher.encode(aula.getId()), // id
                    estudioDto,                    // estudio
                    aula.getDuracaoMinutos(),      // duracaoMinutos
                    aula.getDataAula(),            // dataAula
                    aula.getHoraInicio(),          // horaInicio
                    aula.getHoraFim(),             // horaFim
                    idHasher.encode( aula.getCriadoPor().getId()),           // criadoPo (ajusta para o nome correto do campo na Entity)
                    null,                          // idHorario (HorarioTurmaDto - opcional aqui)
                    estadoDto,                      // estado
                    null                            // notas
            );
            return new PagamentoDto(
                    idHasher.encode(pagamento.getId()), // ID seguro para o JS
                    pagamento.getValorPagamento(),
                    pagamento.getPago(),
                    pagamento.getDescricao(),
                    idHasher.encode(pagamento.getIdTipoPagamento().getId()), // Objeto completo
                    nomeTipo,                       // Apenas o nome (String)
                    aulaDtoManual,
                    pagamento.getDataPagamento(),
                    pagamento.getDataConfirmado(),
                    resumo // associa o utilizador ao pagamento
            );
        }

        return new PagamentoDto(
                idHasher.encode(pagamento.getId()), // ID seguro para o JS
                pagamento.getValorPagamento(),
                pagamento.getPago(),
                pagamento.getDescricao(),
                idHasher.encode(pagamento.getIdTipoPagamento().getId()), // Objeto completo
                nomeTipo,                       // Apenas o nome (String)
               null,
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

