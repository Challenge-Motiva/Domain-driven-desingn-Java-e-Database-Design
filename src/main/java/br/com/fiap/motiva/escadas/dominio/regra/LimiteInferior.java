package br.com.fiap.motiva.escadas.dominio.regra;

import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Optional;

/**
 * Regra para sinais que pioram quando DESCEM: nível de óleo do redutor.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class LimiteInferior extends RegraDeLimite {

    private final double limiteAtencao;
    private final double limiteCritico;

    /**
     * @param sinal         sinal avaliado
     * @param limiteAtencao abaixo ou igual a este valor, alerta de atenção
     * @param limiteCritico abaixo ou igual a este valor, alerta crítico
     * @throws ValorInvalidoException se o limite de atenção não for maior que o crítico
     */
    public LimiteInferior(TipoSinal sinal, double limiteAtencao, double limiteCritico) {
        super(sinal);
        if (limiteAtencao <= limiteCritico) {
            throw new ValorInvalidoException(
                    "Para " + sinal + ", o limite de atenção deve ser maior que o crítico.");
        }
        this.limiteAtencao = limiteAtencao;
        this.limiteCritico = limiteCritico;
    }

    @Override
    protected Optional<NivelAlerta> avaliarValor(double valor) {
        if (valor <= limiteCritico) return Optional.of(NivelAlerta.CRITICO);
        if (valor <= limiteAtencao) return Optional.of(NivelAlerta.ATENCAO);
        return Optional.empty();
    }

    @Override
    public double limiteViolado(double valor, NivelAlerta nivel) {
        return nivel == NivelAlerta.CRITICO ? limiteCritico : limiteAtencao;
    }

    @Override
    public String descrever() {
        String u = getSinal().getUnidade();
        return String.format(PT_BR, "%s: atenção abaixo de %.2f %s, crítico abaixo de %.2f %s",
                getSinal(), limiteAtencao, u, limiteCritico, u);
    }
}
