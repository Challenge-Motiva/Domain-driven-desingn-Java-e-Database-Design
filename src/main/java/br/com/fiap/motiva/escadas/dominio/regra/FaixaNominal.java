package br.com.fiap.motiva.escadas.dominio.regra;

import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Optional;

/**
 * Regra para sinais que pioram quando se AFASTAM de um valor nominal, para cima ou
 * para baixo: tensão de alimentação e velocidade dos degraus (desvio, ou "slip").
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class FaixaNominal extends RegraDeLimite {

    private final double valorNominal;
    private final double toleranciaAtencaoPct;
    private final double toleranciaCriticaPct;

    /**
     * @param sinal                sinal avaliado
     * @param valorNominal         valor esperado em operação normal
     * @param toleranciaAtencaoPct desvio percentual que gera alerta de atenção
     * @param toleranciaCriticaPct desvio percentual que gera alerta crítico
     * @throws ValorInvalidoException se o nominal não for positivo ou as tolerâncias forem incoerentes
     */
    public FaixaNominal(TipoSinal sinal, double valorNominal,
                        double toleranciaAtencaoPct, double toleranciaCriticaPct) {
        super(sinal);
        if (valorNominal <= 0) {
            throw new ValorInvalidoException("O valor nominal de " + sinal + " deve ser positivo.");
        }
        if (toleranciaAtencaoPct <= 0 || toleranciaAtencaoPct >= toleranciaCriticaPct) {
            throw new ValorInvalidoException(
                    "Para " + sinal + ", a tolerância de atenção deve ser positiva e menor que a crítica.");
        }
        this.valorNominal = valorNominal;
        this.toleranciaAtencaoPct = toleranciaAtencaoPct;
        this.toleranciaCriticaPct = toleranciaCriticaPct;
    }

    /**
     * @param valor valor medido
     * @return desvio em relação ao nominal, em porcentagem (sempre positivo)
     */
    public double calcularDesvioPct(double valor) {
        return Math.abs(valor - valorNominal) / valorNominal * 100.0;
    }

    @Override
    protected Optional<NivelAlerta> avaliarValor(double valor) {
        double desvio = calcularDesvioPct(valor);
        if (desvio >= toleranciaCriticaPct) return Optional.of(NivelAlerta.CRITICO);
        if (desvio >= toleranciaAtencaoPct) return Optional.of(NivelAlerta.ATENCAO);
        return Optional.empty();
    }

    @Override
    public double limiteViolado(double valor, NivelAlerta nivel) {
        double tolerancia = nivel == NivelAlerta.CRITICO ? toleranciaCriticaPct : toleranciaAtencaoPct;
        double fator = tolerancia / 100.0;
        // devolve a borda da faixa do lado em que o valor saiu
        return valor < valorNominal ? valorNominal * (1 - fator) : valorNominal * (1 + fator);
    }

    @Override
    public String descrever() {
        return String.format(PT_BR, "%s: nominal %.2f %s, atenção com desvio de %.0f%%, crítico com %.0f%%",
                getSinal(), valorNominal, getSinal().getUnidade(), toleranciaAtencaoPct, toleranciaCriticaPct);
    }
}
