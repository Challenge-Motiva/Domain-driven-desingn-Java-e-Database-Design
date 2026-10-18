package br.com.fiap.motiva.escadas.dominio.excecao;

/**
 * Lançada quando uma leitura de sensores não pode ser aceita: valores fora da
 * faixa física, leitura enviada para a escada errada ou fora de ordem cronológica.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class LeituraInvalidaException extends DominioException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem motivo da rejeição da leitura
     */
    public LeituraInvalidaException(String mensagem) {
        super(mensagem);
    }

    /**
     * @param mensagem motivo da rejeição da leitura
     * @param causa    validação original que falhou
     */
    public LeituraInvalidaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
