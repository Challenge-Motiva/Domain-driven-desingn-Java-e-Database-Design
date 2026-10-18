package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Gravidade de um alerta gerado quando um sinal ultrapassa o limite.
 * Cada nível corresponde a uma cor do semáforo da escada.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum NivelAlerta {

    ATENCAO("Atenção", StatusCondicao.ATENCAO),
    CRITICO("Crítico", StatusCondicao.INTERVENCAO_RECOMENDADA);

    private final String descricao;
    private final StatusCondicao statusCorrespondente;

    NivelAlerta(String descricao, StatusCondicao statusCorrespondente) {
        this.descricao = descricao;
        this.statusCorrespondente = statusCorrespondente;
    }

    /** @return nome do nível para exibição */
    public String getDescricao() {
        return descricao;
    }

    /** @return status que a escada assume quando recebe um alerta deste nível */
    public StatusCondicao getStatusCorrespondente() {
        return statusCorrespondente;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
