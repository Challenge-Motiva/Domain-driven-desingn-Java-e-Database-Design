package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.LeituraInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Entidade: um registro dos sinais de uma escada em um instante, equivalente a uma
 * linha do dataset da Motiva ou a uma mensagem publicada pelo ESP32.
 * Uma leitura nunca muda depois de criada.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class Leitura {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long id;
    private final CodigoEscada codigoEscada;
    private final LocalDateTime dataHora;
    private final Medicao vibracao;
    private final Medicao temperatura;
    private final Medicao corrente;
    private final Medicao tensao;
    private final Medicao nivelOleo;
    private final Medicao velocidade;

    /**
     * Leitura nova, como chega do ESP32 ou de uma linha do dataset (valores brutos).
     *
     * @param codigoEscada escada que gerou a leitura
     * @param dataHora     instante da medição
     * @param vibracao     vibração do motor/redutor, em mm/s
     * @param temperatura  temperatura dos componentes, em °C
     * @param corrente     corrente do motor, em A
     * @param tensao       tensão de alimentação, em V
     * @param nivelOleo    nível do reservatório de óleo, em %
     * @param velocidade   velocidade dos degraus, em m/s
     * @throws LeituraInvalidaException se algum dado estiver ausente ou fisicamente impossível
     */
    public Leitura(CodigoEscada codigoEscada, LocalDateTime dataHora,
                   double vibracao, double temperatura, double corrente,
                   double tensao, double nivelOleo, double velocidade) {
        this(null, codigoEscada, dataHora, vibracao, temperatura, corrente, tensao, nivelOleo, velocidade);
    }

    /**
     * Reconstrução de uma leitura já gravada no banco.
     *
     * @param id           identificador no banco
     * @param codigoEscada escada que gerou a leitura
     * @param dataHora     instante da medição
     * @param vibracao     vibração, em mm/s
     * @param temperatura  temperatura, em °C
     * @param corrente     corrente, em A
     * @param tensao       tensão, em V
     * @param nivelOleo    nível de óleo, em %
     * @param velocidade   velocidade, em m/s
     * @throws LeituraInvalidaException se algum dado estiver ausente ou fisicamente impossível
     */
    public Leitura(Long id, CodigoEscada codigoEscada, LocalDateTime dataHora,
                   double vibracao, double temperatura, double corrente,
                   double tensao, double nivelOleo, double velocidade) {
        if (id != null && id <= 0) {
            throw new LeituraInvalidaException("O id da leitura deve ser positivo.");
        }
        if (codigoEscada == null) {
            throw new LeituraInvalidaException("A leitura precisa informar a escada de origem.");
        }
        if (dataHora == null) {
            throw new LeituraInvalidaException("A leitura de " + codigoEscada + " está sem data e hora.");
        }
        if (dataHora.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new LeituraInvalidaException("A leitura de " + codigoEscada
                    + " está no futuro (" + dataHora.format(FORMATO) + "). Verifique o relógio do dispositivo.");
        }
        try {
            this.vibracao = new Medicao(TipoSinal.VIBRACAO, vibracao);
            this.temperatura = new Medicao(TipoSinal.TEMPERATURA, temperatura);
            this.corrente = new Medicao(TipoSinal.CORRENTE, corrente);
            this.tensao = new Medicao(TipoSinal.TENSAO, tensao);
            this.nivelOleo = new Medicao(TipoSinal.NIVEL_OLEO, nivelOleo);
            this.velocidade = new Medicao(TipoSinal.VELOCIDADE, velocidade);
        } catch (ValorInvalidoException e) {
            throw new LeituraInvalidaException("Leitura de " + codigoEscada + " em "
                    + dataHora.format(FORMATO) + " rejeitada: " + e.getMessage(), e);
        }
        this.id = id;
        this.codigoEscada = codigoEscada;
        this.dataHora = dataHora;
    }

    /**
     * @param tipo sinal desejado
     * @return medição desse sinal nesta leitura
     */
    public Medicao getMedicao(TipoSinal tipo) {
        switch (tipo) {
            case VIBRACAO:    return vibracao;
            case TEMPERATURA: return temperatura;
            case CORRENTE:    return corrente;
            case TENSAO:      return tensao;
            case NIVEL_OLEO:  return nivelOleo;
            case VELOCIDADE:  return velocidade;
            default: throw new ValorInvalidoException("Sinal desconhecido: " + tipo);
        }
    }

    /** @return as seis medições desta leitura, na ordem de {@link TipoSinal} */
    public List<Medicao> getMedicoes() {
        return List.of(vibracao, temperatura, corrente, tensao, nivelOleo, velocidade);
    }

    /** @return id no banco, ou null se a leitura ainda não foi gravada */
    public Long getId() {
        return id;
    }

    /** @return escada que gerou a leitura */
    public CodigoEscada getCodigoEscada() {
        return codigoEscada;
    }

    /** @return instante da medição */
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return "Leitura{escada=" + codigoEscada
                + ", dataHora=" + dataHora.format(FORMATO)
                + ", " + vibracao
                + ", " + temperatura
                + ", " + corrente
                + ", " + tensao
                + ", " + nivelOleo
                + ", " + velocidade
                + "}";
    }
}
