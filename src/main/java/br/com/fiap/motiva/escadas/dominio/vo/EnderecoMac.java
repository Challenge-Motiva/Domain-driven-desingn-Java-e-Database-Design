package br.com.fiap.motiva.escadas.dominio.vo;

import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.Locale;
import java.util.Objects;

/**
 * Value Object: endereço MAC que identifica um ESP32 na rede da estação.
 * Aceita separador ":" ou "-" e normaliza para AA:BB:CC:DD:EE:FF.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class EnderecoMac {

    private final String valor;

    /**
     * @param valor endereço MAC com 6 pares hexadecimais
     * @throws ValorInvalidoException se o endereço estiver vazio ou fora do formato
     */
    public EnderecoMac(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("O endereço MAC do dispositivo é obrigatório.");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT).replace('-', ':');
        if (!normalizado.matches("([0-9A-F]{2}:){5}[0-9A-F]{2}")) {
            throw new ValorInvalidoException(
                    "Endereço MAC inválido: '" + valor + "'. Formato esperado: AA:BB:CC:DD:EE:FF.");
        }
        this.valor = normalizado;
    }

    /** @return endereço normalizado, ex.: 24:6F:28:AB:10:01 */
    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnderecoMac)) return false;
        return valor.equals(((EnderecoMac) o).valor);
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
