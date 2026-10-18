package br.com.fiap.motiva.escadas.dominio.regra;

import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conjunto de regras de limite, uma para cada sinal monitorado. É o "motor de regras"
 * do MVP: o sistema não usa machine learning, usa limites explicáveis.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class PoliticaDeLimites {

    private final Map<TipoSinal, RegraDeLimite> regras;

    /**
     * @param regras exatamente uma regra para cada {@link TipoSinal}
     * @throws ValorInvalidoException se faltar regra para algum sinal ou houver regra repetida
     */
    public PoliticaDeLimites(List<RegraDeLimite> regras) {
        if (regras == null) {
            throw new ValorInvalidoException("A política precisa de uma lista de regras.");
        }
        Map<TipoSinal, RegraDeLimite> mapa = new EnumMap<>(TipoSinal.class);
        for (RegraDeLimite regra : regras) {
            if (regra == null) {
                throw new ValorInvalidoException("A lista de regras contém um item nulo.");
            }
            if (mapa.put(regra.getSinal(), regra) != null) {
                throw new ValorInvalidoException("Há mais de uma regra para " + regra.getSinal() + ".");
            }
        }
        Set<TipoSinal> faltando = EnumSet.allOf(TipoSinal.class);
        faltando.removeAll(mapa.keySet());
        if (!faltando.isEmpty()) {
            throw new ValorInvalidoException("Faltam regras para: " + faltando + ".");
        }
        this.regras = Collections.unmodifiableMap(mapa);
    }

    /**
     * Limites PROVISÓRIOS, usados até a análise exploratória (Data Science) do dataset
     * da Motiva definir os valores reais. Não representam norma técnica nem dado do cliente.
     *
     * @return política com um limite provisório para cada sinal
     */
    public static PoliticaDeLimites provisoria() {
        List<RegraDeLimite> lista = new ArrayList<>();
        lista.add(new LimiteSuperior(TipoSinal.VIBRACAO, 4.5, 7.1));
        lista.add(new LimiteSuperior(TipoSinal.TEMPERATURA, 70, 85));
        lista.add(new LimiteSuperior(TipoSinal.CORRENTE, 25, 30));
        lista.add(new FaixaNominal(TipoSinal.TENSAO, 380, 5, 10));
        lista.add(new LimiteInferior(TipoSinal.NIVEL_OLEO, 40, 20));
        lista.add(new FaixaNominal(TipoSinal.VELOCIDADE, 0.5, 5, 10));
        return new PoliticaDeLimites(lista);
    }

    /**
     * @param sinal sinal desejado
     * @return regra aplicada a esse sinal
     */
    public RegraDeLimite regraPara(TipoSinal sinal) {
        return regras.get(sinal);
    }

    /** @return todas as regras, em ordem de sinal (somente leitura) */
    public List<RegraDeLimite> getRegras() {
        return List.copyOf(regras.values());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PoliticaDeLimites:");
        for (RegraDeLimite regra : regras.values()) {
            sb.append(System.lineSeparator()).append("  - ").append(regra.descrever());
        }
        return sb.toString();
    }
}
