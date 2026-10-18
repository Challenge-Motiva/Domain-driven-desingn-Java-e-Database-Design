package br.com.fiap.motiva.escadas.dominio.vo;

import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Locale;
import java.util.Objects;

/**
 * Value Object: o valor de um sinal medido por um sensor, com seu tipo e unidade.
 * Garante que nenhum número sem sentido físico (negativo, infinito, acima do
 * máximo plausível) entre no sistema.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class Medicao {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final TipoSinal tipo;
    private final double valor;

    /**
     * @param tipo  sinal medido
     * @param valor valor lido, na unidade do sinal
     * @throws ValorInvalidoException se o tipo for nulo ou o valor estiver fora da faixa física
     */
    public Medicao(TipoSinal tipo, double valor) {
        if (tipo == null) {
            throw new ValorInvalidoException("O tipo do sinal é obrigatório.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new ValorInvalidoException(tipo.getDescricao() + ": valor não numérico.");
        }
        if (!tipo.isPlausivel(valor)) {
            throw new ValorInvalidoException(String.format(PT_BR,
                    "%s fora da faixa física: %.2f %s (aceito de %.2f a %.2f). Provável falha do sensor.",
                    tipo.getDescricao(), valor, tipo.getUnidade(), tipo.getMinimoFisico(), tipo.getMaximoFisico()));
        }
        this.tipo = tipo;
        this.valor = valor;
    }

    /** @return sinal medido */
    public TipoSinal getTipo() {
        return tipo;
    }

    /** @return valor numérico, na unidade do sinal */
    public double getValor() {
        return valor;
    }

    /** @return valor com unidade, ex.: "4,20 mm/s" */
    public String formatada() {
        return String.format(PT_BR, "%.2f %s", valor, tipo.getUnidade());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medicao)) return false;
        Medicao outra = (Medicao) o;
        return tipo == outra.tipo && Double.compare(valor, outra.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, valor);
    }

    @Override
    public String toString() {
        return tipo.getDescricao() + "=" + formatada();
    }
}
