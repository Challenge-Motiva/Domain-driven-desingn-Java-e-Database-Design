package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Os 4 grupos de manutenção usados pela Motiva, diferenciados pelo desnível do
 * equipamento. Na ViaQuatro: 47 Críticas A, 35 Críticas B, 94 Não críticas A
 * e 21 Não críticas B (197 escadas).
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum GrupoManutencao {

    CRITICA_A("Crítica A", true),
    CRITICA_B("Crítica B", true),
    NAO_CRITICA_A("Não crítica A", false),
    NAO_CRITICA_B("Não crítica B", false);

    private final String descricao;
    private final boolean critica;

    GrupoManutencao(String descricao, boolean critica) {
        this.descricao = descricao;
        this.critica = critica;
    }

    /** @return nome do grupo para exibição */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Escadas críticas têm prioridade sobre não críticas no mesmo nível de alerta (RN01).
     *
     * @return true se o grupo é crítico
     */
    public boolean isCritica() {
        return critica;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
