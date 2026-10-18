package br.com.fiap.motiva.escadas.dominio.excecao;

/**
 * Lançada quando se tenta mudar o status de um alerta ou de uma ordem de serviço
 * por um caminho que o processo de manutenção não permite
 * (por exemplo, confirmar um alerta que já foi descartado).
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TransicaoDeStatusInvalidaException extends DominioException {

    private static final long serialVersionUID = 1L;

    /**
     * @param entidade nome da entidade (ex.: "Alerta AL-12")
     * @param atual    status em que a entidade está
     * @param desejado status para o qual se tentou mudar
     */
    public TransicaoDeStatusInvalidaException(String entidade, Enum<?> atual, Enum<?> desejado) {
        super(entidade + " não pode passar de " + atual + " para " + desejado + ".");
    }
}
