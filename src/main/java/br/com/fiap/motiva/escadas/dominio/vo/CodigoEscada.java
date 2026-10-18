package br.com.fiap.motiva.escadas.dominio.vo;

import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: código de uma escada rolante no padrão ECR-XXX-00, em que XXX é a
 * sigla da estação e 00 é o número da escada na estação (ex.: ECR-PAU-03).
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class CodigoEscada {

    private static final Pattern FORMATO = Pattern.compile("ECR-[A-Z]{3}-\\d{2}");

    private final String valor;

    /**
     * Usado quando o código já existe (vindo do dataset ou do ESP32).
     *
     * @param valor código no formato ECR-XXX-00
     * @throws ValorInvalidoException se o código estiver vazio ou fora do formato
     */
    public CodigoEscada(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("O código da escada é obrigatório.");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(normalizado).matches()) {
            throw new ValorInvalidoException(
                    "Código de escada inválido: '" + valor + "'. Formato esperado: ECR-XXX-00 (ex.: ECR-PAU-03).");
        }
        if (normalizado.endsWith("-00")) {
            throw new ValorInvalidoException("O número da escada começa em 01: '" + valor + "'.");
        }
        this.valor = normalizado;
    }

    /**
     * Usado ao cadastrar uma escada nova: monta o código a partir da sigla e do número.
     *
     * @param sigla  sigla da estação onde a escada está
     * @param numero número da escada dentro da estação, de 1 a 99
     * @throws ValorInvalidoException se a sigla for nula ou o número estiver fora de 1 a 99
     */
    public CodigoEscada(SiglaEstacao sigla, int numero) {
        this(montar(sigla, numero));
    }

    private static String montar(SiglaEstacao sigla, int numero) {
        if (sigla == null) {
            throw new ValorInvalidoException("A sigla da estação é obrigatória para gerar o código.");
        }
        if (numero < 1 || numero > 99) {
            throw new ValorInvalidoException("O número da escada deve estar entre 1 e 99. Recebido: " + numero);
        }
        return String.format("ECR-%s-%02d", sigla.getValor(), numero);
    }

    /** @return código completo, ex.: ECR-PAU-03 */
    public String getValor() {
        return valor;
    }

    /** @return sigla da estação contida no código, ex.: PAU */
    public SiglaEstacao getSiglaEstacao() {
        return new SiglaEstacao(valor.substring(4, 7));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoEscada)) return false;
        return valor.equals(((CodigoEscada) o).valor);
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
