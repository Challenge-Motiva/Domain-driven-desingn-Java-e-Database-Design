package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Sinais monitorados em cada escada, conforme o dataset descrito pela Motiva.
 * <p>
 * A faixa mínima e máxima de cada sinal NÃO é o limite de alerta: é a faixa de
 * plausibilidade física. Um valor fora dela indica falha do sensor ou erro de
 * transmissão, e a leitura é rejeitada. Valores PROVISÓRIOS, a revisar com a
 * análise exploratória do dataset.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum TipoSinal {

    VIBRACAO("Vibração", "mm/s", 0, 100),
    TEMPERATURA("Temperatura", "°C", -20, 150),
    CORRENTE("Corrente", "A", 0, 200),
    TENSAO("Tensão", "V", 0, 500),
    NIVEL_OLEO("Nível de óleo", "%", 0, 100),
    VELOCIDADE("Velocidade", "m/s", 0, 2);

    private final String descricao;
    private final String unidade;
    private final double minimoFisico;
    private final double maximoFisico;

    TipoSinal(String descricao, String unidade, double minimoFisico, double maximoFisico) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.minimoFisico = minimoFisico;
        this.maximoFisico = maximoFisico;
    }

    /** @return nome do sinal para exibição */
    public String getDescricao() {
        return descricao;
    }

    /** @return unidade de medida (ex.: "mm/s") */
    public String getUnidade() {
        return unidade;
    }

    /** @return menor valor fisicamente plausível */
    public double getMinimoFisico() {
        return minimoFisico;
    }

    /** @return maior valor fisicamente plausível */
    public double getMaximoFisico() {
        return maximoFisico;
    }

    /**
     * @param valor valor lido pelo sensor
     * @return true se o valor está dentro da faixa física plausível
     */
    public boolean isPlausivel(double valor) {
        return valor >= minimoFisico && valor <= maximoFisico;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
