package br.com.fiap.motiva.escadas.dominio.excecao;

/**
 * Lançada quando se tenta acionar uma equipe (abrir ordem de serviço) para um alerta
 * que ainda não foi confirmado pelo gestor. Garante a regra de negócio RN02:
 * o sistema recomenda, mas quem decide o acionamento é sempre uma pessoa.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class AcionamentoNaoAutorizadoException extends DominioException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem explicação de por que o acionamento foi bloqueado
     */
    public AcionamentoNaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
