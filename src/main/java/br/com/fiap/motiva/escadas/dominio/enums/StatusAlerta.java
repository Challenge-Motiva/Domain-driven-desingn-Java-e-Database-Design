package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Ciclo de vida de um alerta: nasce ABERTO e só o gestor pode confirmá-lo ou descartá-lo.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum StatusAlerta {

    ABERTO("Aberto, aguardando decisão do gestor"),
    CONFIRMADO("Confirmado pelo gestor"),
    DESCARTADO("Descartado pelo gestor");

    private final String descricao;

    StatusAlerta(String descricao) {
        this.descricao = descricao;
    }

    /** @return explicação do status */
    public String getDescricao() {
        return descricao;
    }

    /** @return true se o gestor já tomou uma decisão sobre o alerta */
    public boolean isDecidido() {
        return this != ABERTO;
    }
}
