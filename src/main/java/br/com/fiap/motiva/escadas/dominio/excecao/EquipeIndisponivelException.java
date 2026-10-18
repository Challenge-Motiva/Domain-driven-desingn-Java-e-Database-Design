package br.com.fiap.motiva.escadas.dominio.excecao;

/**
 * Lançada quando se tenta alocar uma equipe que já está atendendo outra ordem de serviço.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class EquipeIndisponivelException extends DominioException {

    private static final long serialVersionUID = 1L;

    /**
     * @param nomeEquipe nome da equipe que já está ocupada
     */
    public EquipeIndisponivelException(String nomeEquipe) {
        super("A equipe '" + nomeEquipe + "' já está alocada em outra ordem de serviço.");
    }
}
