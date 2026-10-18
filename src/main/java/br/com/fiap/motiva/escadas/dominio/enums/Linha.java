package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Linhas metroferroviárias de São Paulo operadas pelo grupo Motiva.
 * É um conjunto fixo, por isso é uma enumeração e não uma classe.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum Linha {

    LINHA_4_AMARELA("4", "Amarela", "ViaQuatro"),
    LINHA_5_LILAS("5", "Lilás", "ViaMobilidade"),
    LINHA_8_DIAMANTE("8", "Diamante", "ViaMobilidade"),
    LINHA_9_ESMERALDA("9", "Esmeralda", "ViaMobilidade");

    private final String numero;
    private final String cor;
    private final String concessionaria;

    Linha(String numero, String cor, String concessionaria) {
        this.numero = numero;
        this.cor = cor;
        this.concessionaria = concessionaria;
    }

    /** @return número da linha (ex.: "4") */
    public String getNumero() {
        return numero;
    }

    /** @return cor que dá nome à linha (ex.: "Amarela") */
    public String getCor() {
        return cor;
    }

    /** @return concessionária responsável pela operação */
    public String getConcessionaria() {
        return concessionaria;
    }

    /** @return nome legível, ex.: "Linha 4-Amarela" */
    @Override
    public String toString() {
        return "Linha " + numero + "-" + cor;
    }
}
