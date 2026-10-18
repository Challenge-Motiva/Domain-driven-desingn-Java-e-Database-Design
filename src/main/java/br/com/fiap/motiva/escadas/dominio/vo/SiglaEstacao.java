package br.com.fiap.motiva.escadas.dominio.vo;

import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Locale;
import java.util.Objects;

/**
 * Value Object: sigla de 3 letras que identifica uma estação (ex.: PAU para Paulista).
 * É imutável, se valida na criação e é comparado pelo valor, não pela referência.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class SiglaEstacao {

    private final String valor;

    /**
     * @param valor sigla com 3 letras; espaços e minúsculas são normalizados
     * @throws ValorInvalidoException se a sigla for vazia ou não tiver exatamente 3 letras
     */
    public SiglaEstacao(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("A sigla da estação é obrigatória.");
        }
        String normalizada = valor.trim().toUpperCase(Locale.ROOT);
        if (!normalizada.matches("[A-Z]{3}")) {
            throw new ValorInvalidoException(
                    "Sigla de estação inválida: '" + valor + "'. Use exatamente 3 letras, sem acento (ex.: PAU).");
        }
        this.valor = normalizada;
    }

    /** @return sigla normalizada em maiúsculas */
    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiglaEstacao)) return false;
        return valor.equals(((SiglaEstacao) o).valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
