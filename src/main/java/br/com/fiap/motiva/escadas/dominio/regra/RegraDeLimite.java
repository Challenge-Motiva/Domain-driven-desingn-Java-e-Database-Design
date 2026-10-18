package br.com.fiap.motiva.escadas.dominio.regra;

import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;

import java.util.Locale;
import java.util.Optional;

/**
 * Regra que diz se a medição de um sinal está normal, em atenção ou crítica.
 * <p>
 * É abstrata porque cada sinal "piora" de um jeito: vibração piora quando sobe,
 * nível de óleo piora quando desce e velocidade piora quando se afasta do valor
 * nominal. Cada subclasse implementa {@link #avaliarValor(double)} do seu jeito
 * (polimorfismo), e quem usa a regra não precisa saber qual é.
 * <p>
 * As regras são imutáveis e validadas na criação, como Value Objects.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public abstract class RegraDeLimite {

    /** Locale usado para exibir números com vírgula decimal. */
    protected static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final TipoSinal sinal;

    /**
     * @param sinal sinal ao qual a regra se aplica
     * @throws ValorInvalidoException se o sinal for nulo
     */
    protected RegraDeLimite(TipoSinal sinal) {
        if (sinal == null) {
            throw new ValorInvalidoException("Toda regra de limite precisa de um sinal.");
        }
        this.sinal = sinal;
    }

    /** @return sinal ao qual a regra se aplica */
    public TipoSinal getSinal() {
        return sinal;
    }

    /**
     * Avalia uma medição. Método final: garante que a regra só avalia o próprio sinal
     * e delega o cálculo à subclasse.
     *
     * @param medicao medição a avaliar
     * @return nível do alerta, ou vazio se o valor está normal
     * @throws ValorInvalidoException se a medição for nula ou de outro sinal
     */
    public final Optional<NivelAlerta> avaliar(Medicao medicao) {
        if (medicao == null) {
            throw new ValorInvalidoException("Não há medição para avaliar.");
        }
        if (medicao.getTipo() != sinal) {
            throw new ValorInvalidoException(
                    "A regra de " + sinal + " não pode avaliar uma medição de " + medicao.getTipo() + ".");
        }
        return avaliarValor(medicao.getValor());
    }

    /**
     * @param valor valor medido
     * @return nível do alerta, ou vazio se normal
     */
    protected abstract Optional<NivelAlerta> avaliarValor(double valor);

    /**
     * Informa qual limite foi ultrapassado, para registrar a evidência no alerta.
     *
     * @param valor valor medido
     * @param nivel nível atingido
     * @return valor do limite ultrapassado, na unidade do sinal
     */
    public abstract double limiteViolado(double valor, NivelAlerta nivel);

    /** @return descrição da regra em linguagem do negócio */
    public abstract String descrever();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + descrever() + "]";
    }
}
