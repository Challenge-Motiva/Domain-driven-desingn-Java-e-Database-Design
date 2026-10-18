package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.enums.StatusOrdemServico;
import br.com.fiap.motiva.escadas.dominio.excecao.AcionamentoNaoAutorizadoException;
import br.com.fiap.motiva.escadas.dominio.excecao.EquipeIndisponivelException;
import br.com.fiap.motiva.escadas.dominio.excecao.TransicaoDeStatusInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Entidade: ordem de serviço que aciona uma equipe para atender um alerta.
 * <p>
 * Só pode ser aberta para um alerta confirmado pelo gestor (RN02). Ao abrir,
 * a equipe é alocada; ao concluir ou cancelar, a equipe é liberada.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class OrdemServico {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TAMANHO_MINIMO_TEXTO = 10;
    /** Igual ao tamanho da coluna DS_DIAGNOSTICO no banco. */
    private static final int TAMANHO_MAXIMO_TEXTO = 500;
    private static final String PREFIXO_CANCELAMENTO = "Cancelada: ";

    private Long id;
    private final Alerta alerta;
    private final Equipe equipe;
    private StatusOrdemServico status;
    private final LocalDateTime abertaEm;
    private LocalDateTime iniciadaEm;
    private LocalDateTime encerradaEm;
    private String diagnostico;

    /**
     * Abre uma ordem de serviço e aloca a equipe.
     *
     * @param alerta   alerta que motivou a OS; precisa estar CONFIRMADO
     * @param equipe   equipe acionada; precisa estar disponível
     * @param abertaEm instante da abertura
     * @throws AcionamentoNaoAutorizadoException se o alerta não foi confirmado pelo gestor
     * @throws EquipeIndisponivelException       se a equipe já estiver alocada
     * @throws ValorInvalidoException            se faltar dado ou a data for incoerente
     */
    public OrdemServico(Alerta alerta, Equipe equipe, LocalDateTime abertaEm) {
        if (alerta == null || equipe == null || abertaEm == null) {
            throw new ValorInvalidoException("Alerta, equipe e data de abertura são obrigatórios.");
        }
        if (!alerta.isConfirmado()) {
            throw new AcionamentoNaoAutorizadoException("Nenhuma equipe é acionada sem aprovação do gestor: o alerta de "
                    + alerta.getSinal() + " em " + alerta.getCodigoEscada() + " está " + alerta.getStatus() + ".");
        }
        if (abertaEm.isBefore(alerta.getDecididoEm())) {
            throw new ValorInvalidoException("A OS não pode ser aberta antes da confirmação do alerta.");
        }
        equipe.alocar();
        this.alerta = alerta;
        this.equipe = equipe;
        this.abertaEm = abertaEm;
        this.status = StatusOrdemServico.ABERTA;
    }

    /**
     * Reconstrução de uma OS já gravada. Não aloca a equipe de novo.
     *
     * @param id          identificador no banco
     * @param alerta      alerta que motivou a OS
     * @param equipe      equipe acionada
     * @param status      status atual
     * @param abertaEm    instante da abertura
     * @param iniciadaEm  início do atendimento, ou null
     * @param encerradaEm conclusão ou cancelamento, ou null
     * @param diagnostico diagnóstico do técnico ou motivo do cancelamento, ou null
     * @throws ValorInvalidoException se os dados forem incoerentes com o status
     */
    public OrdemServico(Long id, Alerta alerta, Equipe equipe, StatusOrdemServico status, LocalDateTime abertaEm,
                        LocalDateTime iniciadaEm, LocalDateTime encerradaEm, String diagnostico) {
        if (id == null || id <= 0) {
            throw new ValorInvalidoException("Uma OS reconstruída do banco precisa de id positivo.");
        }
        if (alerta == null || equipe == null || status == null || abertaEm == null) {
            throw new ValorInvalidoException("Alerta, equipe, status e data de abertura são obrigatórios.");
        }
        if (status.isEncerrada() && (encerradaEm == null || diagnostico == null)) {
            throw new ValorInvalidoException("Uma OS encerrada precisa de data de encerramento e diagnóstico.");
        }
        if (status == StatusOrdemServico.EM_ATENDIMENTO && iniciadaEm == null) {
            throw new ValorInvalidoException("Uma OS em atendimento precisa da data de início.");
        }
        this.id = id;
        this.alerta = alerta;
        this.equipe = equipe;
        this.status = status;
        this.abertaEm = abertaEm;
        this.iniciadaEm = iniciadaEm;
        this.encerradaEm = encerradaEm;
        this.diagnostico = diagnostico;
    }

    /**
     * A equipe chegou à escada e começou o atendimento.
     *
     * @param quando instante do início
     * @throws TransicaoDeStatusInvalidaException se a OS não estiver ABERTA
     * @throws ValorInvalidoException             se o início for anterior à abertura
     */
    public void iniciarAtendimento(LocalDateTime quando) {
        validarTransicao(StatusOrdemServico.EM_ATENDIMENTO);
        if (quando == null || quando.isBefore(abertaEm)) {
            throw new ValorInvalidoException("O atendimento não pode começar antes da abertura da OS.");
        }
        this.iniciadaEm = quando;
        this.status = StatusOrdemServico.EM_ATENDIMENTO;
    }

    /**
     * O técnico terminou o serviço. A equipe é liberada.
     *
     * @param quando      instante da conclusão
     * @param diagnostico o que foi encontrado e feito
     * @throws TransicaoDeStatusInvalidaException se a OS não estiver EM_ATENDIMENTO
     * @throws ValorInvalidoException             se o diagnóstico for curto ou a data incoerente
     */
    public void concluir(LocalDateTime quando, String diagnostico) {
        validarTransicao(StatusOrdemServico.CONCLUIDA);
        if (quando == null || quando.isBefore(iniciadaEm)) {
            throw new ValorInvalidoException("A conclusão não pode ser anterior ao início do atendimento.");
        }
        this.diagnostico = validarTexto(diagnostico, "O diagnóstico", TAMANHO_MAXIMO_TEXTO);
        this.encerradaEm = quando;
        this.status = StatusOrdemServico.CONCLUIDA;
        equipe.liberar();
    }

    /**
     * Cancela a OS (ex.: alerta resolvido por outra intervenção). A equipe é liberada.
     *
     * @param quando instante do cancelamento
     * @param motivo motivo do cancelamento
     * @throws TransicaoDeStatusInvalidaException se a OS já estiver encerrada
     * @throws ValorInvalidoException             se o motivo for curto ou a data incoerente
     */
    public void cancelar(LocalDateTime quando, String motivo) {
        validarTransicao(StatusOrdemServico.CANCELADA);
        if (quando == null || quando.isBefore(abertaEm)) {
            throw new ValorInvalidoException("O cancelamento não pode ser anterior à abertura da OS.");
        }
        this.diagnostico = PREFIXO_CANCELAMENTO + validarTexto(motivo, "O motivo do cancelamento",
                TAMANHO_MAXIMO_TEXTO - PREFIXO_CANCELAMENTO.length());
        this.encerradaEm = quando;
        this.status = StatusOrdemServico.CANCELADA;
        equipe.liberar();
    }

    /**
     * Tempo entre a abertura e a conclusão; é a base do indicador MTTR do dashboard.
     *
     * @return duração do reparo, ou vazio se a OS não foi concluída
     */
    public Optional<Duration> calcularTempoDeReparo() {
        if (status != StatusOrdemServico.CONCLUIDA) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(abertaEm, encerradaEm));
    }

    private void validarTransicao(StatusOrdemServico destino) {
        if (!status.permiteTransicaoPara(destino)) {
            throw new TransicaoDeStatusInvalidaException(
                    "Ordem de serviço" + (id != null ? " #" + id : ""), status, destino);
        }
    }

    private static String validarTexto(String texto, String campo, int tamanhoMaximo) {
        if (texto == null || texto.trim().length() < TAMANHO_MINIMO_TEXTO) {
            throw new ValorInvalidoException(campo + " deve ter pelo menos " + TAMANHO_MINIMO_TEXTO + " caracteres.");
        }
        if (texto.trim().length() > tamanhoMaximo) {
            throw new ValorInvalidoException(campo + " deve ter no máximo " + tamanhoMaximo + " caracteres.");
        }
        return texto.trim();
    }

    /** @return id no banco, ou null se ainda não foi gravada */
    public Long getId() {
        return id;
    }

    /** @return alerta que motivou a OS */
    public Alerta getAlerta() {
        return alerta;
    }

    /** @return equipe acionada */
    public Equipe getEquipe() {
        return equipe;
    }

    /** @return status atual */
    public StatusOrdemServico getStatus() {
        return status;
    }

    /** @return instante da abertura */
    public LocalDateTime getAbertaEm() {
        return abertaEm;
    }

    /** @return início do atendimento, ou null */
    public LocalDateTime getIniciadaEm() {
        return iniciadaEm;
    }

    /** @return conclusão ou cancelamento, ou null */
    public LocalDateTime getEncerradaEm() {
        return encerradaEm;
    }

    /** @return diagnóstico ou motivo do cancelamento, ou null */
    public String getDiagnostico() {
        return diagnostico;
    }

    @Override
    public String toString() {
        return "OrdemServico{escada=" + alerta.getCodigoEscada()
                + ", motivo='" + alerta.descreverEvidencia() + "'"
                + ", equipe='" + equipe.getNome() + "'"
                + ", status=" + status.getDescricao()
                + ", abertaEm=" + abertaEm.format(FORMATO)
                + (iniciadaEm != null ? ", iniciadaEm=" + iniciadaEm.format(FORMATO) : "")
                + (encerradaEm != null ? ", encerradaEm=" + encerradaEm.format(FORMATO) : "")
                + (diagnostico != null ? ", diagnostico='" + diagnostico + "'" : "")
                + "}";
    }
}
