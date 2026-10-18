package br.com.fiap.motiva.escadas.dominio.enums;

/**
 * Fabricantes de escadas rolantes. Registrar o fabricante é o que permite à
 * solução ser multimarca: as plataformas dos próprios fabricantes só monitoram
 * os equipamentos da sua marca.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum Fabricante {

    OTIS("Otis"),
    KONE("KONE"),
    SCHINDLER("Schindler"),
    TK_ELEVATOR("TK Elevator"),
    OUTRO("Outro");

    private final String nome;

    Fabricante(String nome) {
        this.nome = nome;
    }

    /** @return nome comercial do fabricante */
    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
