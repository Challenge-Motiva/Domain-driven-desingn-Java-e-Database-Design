package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.enums.Turno;
import br.com.fiap.motiva.escadas.dominio.excecao.EquipeIndisponivelException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.time.LocalTime;

/**
 * Entidade: equipe de técnicos de manutenção que pode ser acionada para atender
 * uma ordem de serviço. Fica sediada em uma estação-base, ponto de partida das rotas.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class Equipe {

    /** Limite de técnicos por equipe, igual à restrição da coluna no banco. */
    public static final int MAXIMO_TECNICOS = 20;

    private Long id;
    private final String nome;
    private final Turno turno;
    private final Estacao base;
    private int quantidadeTecnicos;
    private boolean disponivel;

    /**
     * Cadastro de uma equipe nova, que começa disponível.
     *
     * @param nome               nome da equipe
     * @param turno              turno de trabalho
     * @param base               estação-base da equipe
     * @param quantidadeTecnicos número de técnicos, de 1 a 20
     * @throws ValorInvalidoException se faltar dado ou a quantidade for inválida
     */
    public Equipe(String nome, Turno turno, Estacao base, int quantidadeTecnicos) {
        this(null, nome, turno, base, quantidadeTecnicos, true);
    }

    /**
     * Reconstrução de uma equipe já cadastrada.
     *
     * @param id                 identificador no banco
     * @param nome               nome da equipe
     * @param turno              turno de trabalho
     * @param base               estação-base
     * @param quantidadeTecnicos número de técnicos
     * @param disponivel         se a equipe está livre para ser acionada
     * @throws ValorInvalidoException se faltar dado ou a quantidade for inválida
     */
    public Equipe(Long id, String nome, Turno turno, Estacao base, int quantidadeTecnicos, boolean disponivel) {
        if (id != null && id <= 0) {
            throw new ValorInvalidoException("O id da equipe deve ser positivo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new ValorInvalidoException("O nome da equipe é obrigatório.");
        }
        if (nome.trim().length() > 60) {
            throw new ValorInvalidoException("O nome da equipe deve ter no máximo 60 caracteres.");
        }
        if (turno == null || base == null) {
            throw new ValorInvalidoException("Turno e estação-base da equipe são obrigatórios.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.turno = turno;
        this.base = base;
        this.quantidadeTecnicos = validarQuantidade(quantidadeTecnicos);
        this.disponivel = disponivel;
    }

    /**
     * Reserva a equipe para uma ordem de serviço.
     *
     * @throws EquipeIndisponivelException se a equipe já estiver alocada
     */
    public void alocar() {
        if (!disponivel) {
            throw new EquipeIndisponivelException(nome);
        }
        this.disponivel = false;
    }

    /** Libera a equipe ao concluir ou cancelar a ordem de serviço. */
    public void liberar() {
        this.disponivel = true;
    }

    /**
     * @param horario horário a verificar
     * @return true se a equipe trabalha nesse horário
     */
    public boolean atuaNoHorario(LocalTime horario) {
        return turno.contem(horario);
    }

    /**
     * Mutador necessário: o tamanho da equipe muda com férias, afastamentos e contratações.
     *
     * @param quantidadeTecnicos número de técnicos, de 1 a 20
     * @throws ValorInvalidoException se a quantidade estiver fora da faixa
     */
    public void setQuantidadeTecnicos(int quantidadeTecnicos) {
        this.quantidadeTecnicos = validarQuantidade(quantidadeTecnicos);
    }

    private static int validarQuantidade(int quantidade) {
        if (quantidade < 1 || quantidade > MAXIMO_TECNICOS) {
            throw new ValorInvalidoException("A equipe deve ter entre 1 e " + MAXIMO_TECNICOS
                    + " técnicos. Recebido: " + quantidade);
        }
        return quantidade;
    }

    /** @return id no banco, ou null se ainda não foi gravada */
    public Long getId() {
        return id;
    }

    /** @return nome da equipe */
    public String getNome() {
        return nome;
    }

    /** @return turno de trabalho */
    public Turno getTurno() {
        return turno;
    }

    /** @return estação-base */
    public Estacao getBase() {
        return base;
    }

    /** @return número de técnicos */
    public int getQuantidadeTecnicos() {
        return quantidadeTecnicos;
    }

    /** @return true se a equipe está livre */
    public boolean isDisponivel() {
        return disponivel;
    }

    @Override
    public String toString() {
        return "Equipe{nome='" + nome + "'"
                + ", turno=" + turno
                + ", base='" + base.getNome() + "'"
                + ", tecnicos=" + quantidadeTecnicos
                + ", disponivel=" + disponivel
                + "}";
    }
}
