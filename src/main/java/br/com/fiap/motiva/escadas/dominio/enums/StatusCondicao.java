package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Condição de uma escada rolante como aparece no dashboard (o "semáforo").
 * O peso ordena a gravidade e é usado no ranking de prioridade.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum StatusCondicao {

    NORMAL("Verde", "Normal", 0),
    SEM_DADOS("Cinza", "Sem dados", 1),
    ATENCAO("Amarelo", "Atenção", 2),
    INTERVENCAO_RECOMENDADA("Vermelho", "Intervenção recomendada", 3);

    private final String cor;
    private final String descricao;
    private final int peso;

    StatusCondicao(String cor, String descricao, int peso) {
        this.cor = cor;
        this.descricao = descricao;
        this.peso = peso;
    }

    /** @return cor do semáforo no dashboard */
    public String getCor() {
        return cor;
    }

    /** @return texto que acompanha a cor (a cor nunca aparece sozinha) */
    public String getDescricao() {
        return descricao;
    }

    /** @return gravidade de 0 (normal) a 3 (intervenção recomendada) */
    public int getPeso() {
        return peso;
    }

    /**
     * @param outro status a comparar
     * @return true se este status é mais grave que o outro
     */
    public boolean isMaisGraveQue(StatusCondicao outro) {
        return this.peso > outro.peso;
    }

    @Override
    public String toString() {
        return cor + " (" + descricao + ")";
    }
}
