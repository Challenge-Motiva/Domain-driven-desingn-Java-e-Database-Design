package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.regra.FaixaNominal;
import br.com.fiap.motiva.escadas.dominio.regra.LimiteInferior;
import br.com.fiap.motiva.escadas.dominio.regra.LimiteSuperior;
import br.com.fiap.motiva.escadas.dominio.regra.PoliticaDeLimites;
import br.com.fiap.motiva.escadas.dominio.regra.RegraDeLimite;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;

import java.util.List;
import java.util.Optional;

/**
 * Driver de teste isolado das regras de limite. Mostra o polimorfismo: as três
 * subclasses respondem ao mesmo método {@code avaliar} de formas diferentes.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteRegrasDeLimite {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("Regras de limite e política");

        RegraDeLimite vibracao = new LimiteSuperior(TipoSinal.VIBRACAO, 4.5, 7.1);
        RegraDeLimite oleo = new LimiteInferior(TipoSinal.NIVEL_OLEO, 40, 20);
        RegraDeLimite velocidade = new FaixaNominal(TipoSinal.VELOCIDADE, 0.5, 5, 10);

        // LimiteSuperior: piora quando sobe
        v.verificarIgual("Vibração 3,0 está normal", Optional.empty(),
                vibracao.avaliar(new Medicao(TipoSinal.VIBRACAO, 3.0)));
        v.verificarIgual("Vibração 5,0 gera atenção", Optional.of(NivelAlerta.ATENCAO),
                vibracao.avaliar(new Medicao(TipoSinal.VIBRACAO, 5.0)));
        v.verificarIgual("Vibração 7,1 (no limite) gera crítico", Optional.of(NivelAlerta.CRITICO),
                vibracao.avaliar(new Medicao(TipoSinal.VIBRACAO, 7.1)));

        // LimiteInferior: piora quando desce
        v.verificarIgual("Óleo 60% está normal", Optional.empty(),
                oleo.avaliar(new Medicao(TipoSinal.NIVEL_OLEO, 60)));
        v.verificarIgual("Óleo 35% gera atenção", Optional.of(NivelAlerta.ATENCAO),
                oleo.avaliar(new Medicao(TipoSinal.NIVEL_OLEO, 35)));
        v.verificarIgual("Óleo 15% gera crítico", Optional.of(NivelAlerta.CRITICO),
                oleo.avaliar(new Medicao(TipoSinal.NIVEL_OLEO, 15)));

        // FaixaNominal: piora quando se afasta do nominal
        v.verificarIgual("Velocidade 0,50 m/s (nominal) está normal", Optional.empty(),
                velocidade.avaliar(new Medicao(TipoSinal.VELOCIDADE, 0.50)));
        v.verificarIgual("Velocidade 0,47 m/s (desvio de 6%) gera atenção", Optional.of(NivelAlerta.ATENCAO),
                velocidade.avaliar(new Medicao(TipoSinal.VELOCIDADE, 0.47)));
        v.verificarIgual("Velocidade 0,56 m/s (desvio de 12%) gera crítico", Optional.of(NivelAlerta.CRITICO),
                velocidade.avaliar(new Medicao(TipoSinal.VELOCIDADE, 0.56)));
        v.verificar("Limite violado abaixo do nominal é 0,45 m/s",
                Math.abs(velocidade.limiteViolado(0.44, NivelAlerta.CRITICO) - 0.45) < 1e-9);

        // Polimorfismo: a mesma chamada, comportamentos diferentes
        List<RegraDeLimite> regras = List.of(vibracao, oleo, velocidade);
        List<Medicao> medicoes = List.of(
                new Medicao(TipoSinal.VIBRACAO, 8.0),
                new Medicao(TipoSinal.NIVEL_OLEO, 10),
                new Medicao(TipoSinal.VELOCIDADE, 0.40));
        boolean todasCriticas = true;
        for (int i = 0; i < regras.size(); i++) {
            Optional<NivelAlerta> nivel = regras.get(i).avaliar(medicoes.get(i));
            todasCriticas &= nivel.equals(Optional.of(NivelAlerta.CRITICO));
            System.out.println("           " + regras.get(i).getClass().getSimpleName() + " avaliou "
                    + medicoes.get(i).formatada() + " como " + nivel.map(Enum::name).orElse("NORMAL"));
        }
        v.verificar("As três regras avaliam pelo mesmo método, cada uma do seu jeito", todasCriticas);

        // Validações das regras
        v.esperarExcecao("LimiteSuperior rejeita atenção maior que crítico", ValorInvalidoException.class,
                () -> new LimiteSuperior(TipoSinal.TEMPERATURA, 90, 70));
        v.esperarExcecao("LimiteInferior rejeita atenção menor que crítico", ValorInvalidoException.class,
                () -> new LimiteInferior(TipoSinal.NIVEL_OLEO, 10, 30));
        v.esperarExcecao("FaixaNominal rejeita nominal zero", ValorInvalidoException.class,
                () -> new FaixaNominal(TipoSinal.TENSAO, 0, 5, 10));
        v.esperarExcecao("Regra de vibração não avalia temperatura", ValorInvalidoException.class,
                () -> vibracao.avaliar(new Medicao(TipoSinal.TEMPERATURA, 60)));

        // Política
        PoliticaDeLimites politica = PoliticaDeLimites.provisoria();
        v.verificarIgual("Política provisória tem uma regra por sinal", TipoSinal.values().length,
                politica.getRegras().size());
        v.esperarExcecao("Política rejeita lista com sinais faltando", ValorInvalidoException.class,
                () -> new PoliticaDeLimites(List.of(vibracao, oleo)));
        v.esperarExcecao("Política rejeita duas regras para o mesmo sinal", ValorInvalidoException.class,
                () -> new PoliticaDeLimites(List.of(vibracao, new LimiteSuperior(TipoSinal.VIBRACAO, 1, 2))));

        return v.concluir();
    }
}
