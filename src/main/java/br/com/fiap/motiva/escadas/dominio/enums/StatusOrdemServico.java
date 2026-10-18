package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Ciclo de vida de uma ordem de serviço. Cada status sabe para quais outros
 * ele pode ir, o que impede, por exemplo, concluir uma OS que nem começou.
 *
 * <pre>
 * ABERTA ──► EM_ATENDIMENTO ──► CONCLUIDA
 *    │              │
 *    └──────────────┴──► CANCELADA
 * </pre>
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum StatusOrdemServico {

    ABERTA("Aberta"),
    EM_ATENDIMENTO("Em atendimento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusOrdemServico(String descricao) {
        this.descricao = descricao;
    }

    /** @return nome do status para exibição */
    public String getDescricao() {
        return descricao;
    }

    /**
     * @param destino status para o qual se deseja ir
     * @return true se a transição é permitida pelo processo de manutenção
     */
    public boolean permiteTransicaoPara(StatusOrdemServico destino) {
        switch (this) {
            case ABERTA:
                return destino == EM_ATENDIMENTO || destino == CANCELADA;
            case EM_ATENDIMENTO:
                return destino == CONCLUIDA || destino == CANCELADA;
            default:
                return false;
        }
    }

    /** @return true se a OS já foi encerrada (concluída ou cancelada) */
    public boolean isEncerrada() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
