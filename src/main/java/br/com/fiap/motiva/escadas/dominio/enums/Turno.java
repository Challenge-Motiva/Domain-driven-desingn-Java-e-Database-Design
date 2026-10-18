package br.com.fiap.motiva.escadas.dominio.enums;

import java.time.LocalTime;

/**
 * Turnos das equipes de manutenção. O turno da noite atravessa a meia-noite
 * e cobre a madrugada, quando a operação comercial está parada.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public enum Turno {

    MANHA("Manhã", LocalTime.of(6, 0), LocalTime.of(14, 0)),
    TARDE("Tarde", LocalTime.of(14, 0), LocalTime.of(22, 0)),
    NOITE("Noite", LocalTime.of(22, 0), LocalTime.of(6, 0));

    private final String descricao;
    private final LocalTime inicio;
    private final LocalTime fim;

    Turno(String descricao, LocalTime inicio, LocalTime fim) {
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
    }

    /** @return nome do turno */
    public String getDescricao() {
        return descricao;
    }

    /** @return horário de início do turno */
    public LocalTime getInicio() {
        return inicio;
    }

    /** @return horário de fim do turno */
    public LocalTime getFim() {
        return fim;
    }

    /**
     * @param horario horário a verificar
     * @return true se o horário pertence a este turno
     */
    public boolean contem(LocalTime horario) {
        if (inicio.isBefore(fim)) {
            return !horario.isBefore(inicio) && horario.isBefore(fim);
        }
        // turno que atravessa a meia-noite
        return !horario.isBefore(inicio) || horario.isBefore(fim);
    }

    @Override
    public String toString() {
        return descricao + " (" + inicio + "–" + fim + ")";
    }
}
