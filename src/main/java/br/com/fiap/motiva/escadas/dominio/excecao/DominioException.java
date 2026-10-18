package br.com.fiap.motiva.escadas.dominio.excecao;

/**
 * Exceção base do domínio. Toda regra de negócio violada no sistema
 * gera uma subclasse desta exceção, o que permite tratá-las em um único ponto
 * quando a camada de Controller for criada nas próximas sprints.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem explicação da regra violada, em linguagem do negócio
     */
    public DominioException(String mensagem) {
        super(mensagem);
    }

    /**
     * @param mensagem explicação da regra violada
     * @param causa    exceção original que levou a esta
     */
    public DominioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
