package br.com.fiap.motiva.escadas.dominio.vo;

import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Objects;

/**
 * Value Object: texto que o gestor escreve ao confirmar ou descartar um alerta.
 * Existe para garantir rastreabilidade: nenhuma decisão é registrada sem um motivo legível.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class Justificativa {

    /** Tamanho mínimo para evitar justificativas vazias como "ok" ou "sim". */
    public static final int TAMANHO_MINIMO = 10;
    /** Tamanho máximo, igual ao da coluna no banco de dados. */
    public static final int TAMANHO_MAXIMO = 500;

    private final String texto;

    /**
     * @param texto motivo da decisão, com 10 a 500 caracteres
     * @throws ValorInvalidoException se o texto estiver vazio, curto ou longo demais
     */
    public Justificativa(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValorInvalidoException("A justificativa é obrigatória.");
        }
        String limpo = texto.trim();
        if (limpo.length() < TAMANHO_MINIMO) {
            throw new ValorInvalidoException(
                    "A justificativa deve ter pelo menos " + TAMANHO_MINIMO + " caracteres. Recebido: '" + limpo + "'.");
        }
        if (limpo.length() > TAMANHO_MAXIMO) {
            throw new ValorInvalidoException(
                    "A justificativa deve ter no máximo " + TAMANHO_MAXIMO + " caracteres. Recebido: " + limpo.length() + ".");
        }
        this.texto = limpo;
    }

    /** @return texto da justificativa, sem espaços nas pontas */
    public String getTexto() {
        return texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Justificativa)) return false;
        return texto.equals(((Justificativa) o).texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto);
    }

    @Override
    public String toString() {
        return "\"" + texto + "\"";
    }
}
