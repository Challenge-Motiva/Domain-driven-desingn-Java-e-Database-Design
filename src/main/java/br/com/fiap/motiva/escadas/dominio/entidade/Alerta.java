package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.StatusAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.TransicaoDeStatusInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.Justificativa;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Entidade: aviso de que um sinal de uma escada ultrapassou o limite.
 * <p>
 * O alerta guarda a evidência (valor medido e limite ultrapassado) e só muda de
 * status por decisão do gestor, sempre com justificativa. É isso que materializa
 * as regras "o sistema recomenda, a pessoa decide" e "toda decisão é rastreável".
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class Alerta {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Long id;
    private final Leitura leituraOrigem;
    private final TipoSinal sinal;
    private final double valorMedido;
    private final double limiteViolado;
    private final NivelAlerta nivel;
    private StatusAlerta status;
    private final LocalDateTime abertoEm;
    private String decididoPor;
    private Justificativa justificativa;
    private LocalDateTime decididoEm;

    /**
     * Alerta novo, gerado pela avaliação de uma leitura. Nasce sempre ABERTO.
     *
     * @param leituraOrigem leitura que disparou o alerta
     * @param medicao       medição que ultrapassou o limite (deve pertencer à leitura)
     * @param nivel         gravidade do alerta
     * @param limiteViolado limite ultrapassado, na unidade do sinal
     * @throws ValorInvalidoException se faltar dado ou a medição não for da leitura informada
     */
    public Alerta(Leitura leituraOrigem, Medicao medicao, NivelAlerta nivel, double limiteViolado) {
        if (leituraOrigem == null || medicao == null || nivel == null) {
            throw new ValorInvalidoException("Leitura, medição e nível são obrigatórios para abrir um alerta.");
        }
        if (!medicao.equals(leituraOrigem.getMedicao(medicao.getTipo()))) {
            throw new ValorInvalidoException("A medição informada não pertence à leitura de origem do alerta.");
        }
        this.leituraOrigem = leituraOrigem;
        this.sinal = medicao.getTipo();
        this.valorMedido = medicao.getValor();
        this.limiteViolado = limiteViolado;
        this.nivel = nivel;
        this.status = StatusAlerta.ABERTO;
        this.abertoEm = leituraOrigem.getDataHora();
    }

    /**
     * Reconstrução de um alerta já gravado no banco, em qualquer status.
     *
     * @param id            identificador no banco
     * @param leituraOrigem leitura que disparou o alerta
     * @param nivel         gravidade
     * @param limiteViolado limite ultrapassado
     * @param sinal         sinal que ultrapassou o limite
     * @param status        status atual
     * @param decididoPor   nome do gestor (obrigatório se o alerta já foi decidido)
     * @param justificativa motivo da decisão (obrigatório se o alerta já foi decidido)
     * @param decididoEm    instante da decisão (obrigatório se o alerta já foi decidido)
     * @throws ValorInvalidoException se os dados forem incoerentes com o status
     */
    public Alerta(Long id, Leitura leituraOrigem, TipoSinal sinal, NivelAlerta nivel, double limiteViolado,
                  StatusAlerta status, String decididoPor, Justificativa justificativa, LocalDateTime decididoEm) {
        this(leituraOrigem, leituraOrigem == null || sinal == null ? null : leituraOrigem.getMedicao(sinal),
                nivel, limiteViolado);
        if (id == null || id <= 0) {
            throw new ValorInvalidoException("Um alerta reconstruído do banco precisa de id positivo.");
        }
        if (status == null) {
            throw new ValorInvalidoException("O status do alerta é obrigatório.");
        }
        boolean temDecisao = decididoPor != null || justificativa != null || decididoEm != null;
        if (status.isDecidido() && (decididoPor == null || justificativa == null || decididoEm == null)) {
            throw new ValorInvalidoException("Alerta " + status + " precisa de gestor, justificativa e data da decisão.");
        }
        if (!status.isDecidido() && temDecisao) {
            throw new ValorInvalidoException("Alerta ABERTO não pode ter dados de decisão.");
        }
        if (decididoEm != null && decididoEm.isBefore(abertoEm)) {
            throw new ValorInvalidoException("A decisão não pode ser anterior à abertura do alerta.");
        }
        this.id = id;
        this.status = status;
        this.decididoPor = decididoPor;
        this.justificativa = justificativa;
        this.decididoEm = decididoEm;
    }

    /**
     * Gestor confirma que o alerta merece intervenção. Só depois disso uma
     * ordem de serviço pode ser aberta.
     *
     * @param gestor        nome de quem decidiu
     * @param justificativa motivo da decisão
     * @param quando        instante da decisão
     * @throws TransicaoDeStatusInvalidaException se o alerta já tiver sido decidido
     * @throws ValorInvalidoException             se faltar gestor, justificativa ou data coerente
     */
    public void confirmar(String gestor, Justificativa justificativa, LocalDateTime quando) {
        decidir(StatusAlerta.CONFIRMADO, gestor, justificativa, quando);
    }

    /**
     * Gestor descarta o alerta (por exemplo, falso positivo já explicado).
     *
     * @param gestor        nome de quem decidiu
     * @param justificativa motivo do descarte
     * @param quando        instante da decisão
     * @throws TransicaoDeStatusInvalidaException se o alerta já tiver sido decidido
     * @throws ValorInvalidoException             se faltar gestor, justificativa ou data coerente
     */
    public void descartar(String gestor, Justificativa justificativa, LocalDateTime quando) {
        decidir(StatusAlerta.DESCARTADO, gestor, justificativa, quando);
    }

    private void decidir(StatusAlerta novoStatus, String gestor, Justificativa justificativa, LocalDateTime quando) {
        if (status != StatusAlerta.ABERTO) {
            throw new TransicaoDeStatusInvalidaException("Alerta " + descreverId(), status, novoStatus);
        }
        if (gestor == null || gestor.isBlank()) {
            throw new ValorInvalidoException("Informe o gestor responsável pela decisão.");
        }
        if (gestor.trim().length() > 80) {
            throw new ValorInvalidoException("O nome do gestor deve ter no máximo 80 caracteres.");
        }
        if (justificativa == null) {
            throw new ValorInvalidoException("Toda decisão sobre um alerta precisa de justificativa.");
        }
        if (quando == null || quando.isBefore(abertoEm)) {
            throw new ValorInvalidoException("A decisão não pode ser anterior à abertura do alerta.");
        }
        this.status = novoStatus;
        this.decididoPor = gestor.trim();
        this.justificativa = justificativa;
        this.decididoEm = quando;
    }

    /** @return true se o alerta ainda aguarda decisão */
    public boolean isAberto() {
        return status == StatusAlerta.ABERTO;
    }

    /** @return true se o gestor confirmou o alerta */
    public boolean isConfirmado() {
        return status == StatusAlerta.CONFIRMADO;
    }

    /**
     * @param agora instante de referência
     * @return tempo desde a abertura até a decisão (ou até agora, se ainda aberto)
     */
    public Duration calcularTempoEmAberto(LocalDateTime agora) {
        LocalDateTime fim = decididoEm != null ? decididoEm : agora;
        return Duration.between(abertoEm, fim);
    }

    /** @return evidência em texto, ex.: "Vibração 7,40 mm/s (limite 7,10 mm/s)" */
    public String descreverEvidencia() {
        return String.format(PT_BR, "%s %.2f %s (limite %.2f %s)",
                sinal.getDescricao(), valorMedido, sinal.getUnidade(), limiteViolado, sinal.getUnidade());
    }

    private String descreverId() {
        return id != null ? "#" + id : "de " + sinal.getDescricao().toLowerCase(PT_BR) + " em " + getCodigoEscada();
    }

    /** @return escada a que o alerta se refere */
    public CodigoEscada getCodigoEscada() {
        return leituraOrigem.getCodigoEscada();
    }

    /** @return id no banco, ou null se ainda não foi gravado */
    public Long getId() {
        return id;
    }

    /** @return leitura que disparou o alerta */
    public Leitura getLeituraOrigem() {
        return leituraOrigem;
    }

    /** @return sinal que ultrapassou o limite */
    public TipoSinal getSinal() {
        return sinal;
    }

    /** @return valor medido */
    public double getValorMedido() {
        return valorMedido;
    }

    /** @return limite ultrapassado */
    public double getLimiteViolado() {
        return limiteViolado;
    }

    /** @return gravidade */
    public NivelAlerta getNivel() {
        return nivel;
    }

    /** @return status atual */
    public StatusAlerta getStatus() {
        return status;
    }

    /** @return instante em que o alerta foi aberto */
    public LocalDateTime getAbertoEm() {
        return abertoEm;
    }

    /** @return nome do gestor que decidiu, ou null */
    public String getDecididoPor() {
        return decididoPor;
    }

    /** @return justificativa da decisão, ou null */
    public Justificativa getJustificativa() {
        return justificativa;
    }

    /** @return instante da decisão, ou null */
    public LocalDateTime getDecididoEm() {
        return decididoEm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Alerta{");
        sb.append("id=").append(id)
          .append(", escada=").append(getCodigoEscada())
          .append(", nivel=").append(nivel)
          .append(", evidencia='").append(descreverEvidencia()).append("'")
          .append(", status=").append(status)
          .append(", abertoEm=").append(abertoEm.format(FORMATO));
        if (status.isDecidido()) {
            sb.append(", decididoPor='").append(decididoPor).append("'")
              .append(", decididoEm=").append(decididoEm.format(FORMATO))
              .append(", justificativa=").append(justificativa);
        }
        return sb.append("}").toString();
    }
}
