package br.com.fiap.motiva.escadas.dominio.excecao;

/**
 * Lançada quando um valor recebido não respeita as regras de formato ou de faixa
 * do domínio (por exemplo, um código de escada fora do padrão ECR-XXX-00).
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class ValorInvalidoException extends DominioException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem descrição do valor inválido e do formato esperado
     */
    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
