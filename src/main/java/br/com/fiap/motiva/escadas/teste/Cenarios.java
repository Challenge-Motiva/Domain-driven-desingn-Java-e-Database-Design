package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.Alerta;
import br.com.fiap.motiva.escadas.dominio.entidade.EscadaRolante;
import br.com.fiap.motiva.escadas.dominio.entidade.Equipe;
import br.com.fiap.motiva.escadas.dominio.entidade.Estacao;
import br.com.fiap.motiva.escadas.dominio.entidade.Leitura;
import br.com.fiap.motiva.escadas.dominio.enums.Fabricante;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.Linha;
import br.com.fiap.motiva.escadas.dominio.enums.Turno;
import br.com.fiap.motiva.escadas.dominio.regra.PoliticaDeLimites;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.Justificativa;
import br.com.fiap.motiva.escadas.dominio.vo.SiglaEstacao;

import java.time.LocalDateTime;

/**
 * Objetos prontos usados pelos testes, para que cada teste comece de um estado
 * conhecido e não dependa de outro. Os valores são fictícios.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class Cenarios {

    /** Instante fixo de referência, para os testes não dependerem do relógio. */
    public static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 15, 8, 0);

    /** Política de limites provisória usada em todos os testes. */
    public static final PoliticaDeLimites POLITICA = PoliticaDeLimites.provisoria();

    private Cenarios() {
    }

    /** @return estação Paulista, Linha 4-Amarela */
    public static Estacao paulista() {
        return new Estacao(1L, "Paulista", new SiglaEstacao("PAU"), Linha.LINHA_4_AMARELA);
    }

    /** @return estação Pinheiros, Linha 4-Amarela */
    public static Estacao pinheiros() {
        return new Estacao(2L, "Pinheiros", new SiglaEstacao("PIN"), Linha.LINHA_4_AMARELA);
    }

    /**
     * @param estacao estação da escada
     * @param numero  número da escada na estação
     * @param grupo   grupo de manutenção
     * @return escada nova, já vinculada à estação
     */
    public static EscadaRolante escada(Estacao estacao, int numero, GrupoManutencao grupo) {
        EscadaRolante escada = new EscadaRolante(new CodigoEscada(estacao.getSigla(), numero), estacao,
                grupo, Fabricante.KONE, "Plataforma → mezanino");
        estacao.adicionarEscada(escada);
        return escada;
    }

    /**
     * @param codigo   escada
     * @param dataHora instante
     * @return leitura com todos os sinais dentro dos limites
     */
    public static Leitura leituraNormal(CodigoEscada codigo, LocalDateTime dataHora) {
        return new Leitura(codigo, dataHora, 2.1, 55.0, 18.0, 380.0, 75.0, 0.50);
    }

    /**
     * @param codigo   escada
     * @param dataHora instante
     * @param vibracao vibração desejada, em mm/s
     * @return leitura normal, exceto pela vibração informada
     */
    public static Leitura leituraComVibracao(CodigoEscada codigo, LocalDateTime dataHora, double vibracao) {
        return new Leitura(codigo, dataHora, vibracao, 55.0, 18.0, 380.0, 75.0, 0.50);
    }

    /**
     * @param escada escada que vai gerar o alerta
     * @return alerta crítico de vibração, ainda ABERTO
     */
    public static Alerta alertaCriticoAberto(EscadaRolante escada) {
        Leitura leitura = leituraComVibracao(escada.getCodigo(), INICIO.plusMinutes(5), 7.4);
        return escada.registrarLeitura(leitura, POLITICA).get(0);
    }

    /**
     * @param escada escada que vai gerar o alerta
     * @return alerta crítico de vibração já CONFIRMADO pelo gestor
     */
    public static Alerta alertaCriticoConfirmado(EscadaRolante escada) {
        Alerta alerta = alertaCriticoAberto(escada);
        alerta.confirmar("Roberto Almeida", new Justificativa("Vibração crítica em escada do grupo A."),
                INICIO.plusMinutes(20));
        return alerta;
    }

    /**
     * @param base estação-base
     * @return equipe do turno da noite, disponível
     */
    public static Equipe equipeNoite(Estacao base) {
        return new Equipe(1L, "Equipe Noturna Paulista", Turno.NOITE, base, 3, true);
    }
}
