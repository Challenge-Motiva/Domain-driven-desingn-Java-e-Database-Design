package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.Leitura;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.LeituraInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;

import java.time.LocalDateTime;

/**
 * Driver de teste isolado da entidade {@link Leitura}.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteLeitura {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("Leitura");
        CodigoEscada codigo = new CodigoEscada("ECR-PAU-03");

        Leitura leitura = new Leitura(codigo, Cenarios.INICIO, 4.2, 61.5, 18.3, 381, 72, 0.49);
        System.out.println("           " + leitura);
        v.verificarIgual("Leitura tem as seis medições", 6, leitura.getMedicoes().size());
        v.verificarIgual("Vibração é recuperada pelo tipo", "4,20 mm/s",
                leitura.getMedicao(TipoSinal.VIBRACAO).formatada());
        v.verificar("Leitura nova não tem id", leitura.getId() == null);

        Leitura doBanco = new Leitura(10L, codigo, Cenarios.INICIO, 4.2, 61.5, 18.3, 381, 72, 0.49);
        v.verificarIgual("Leitura reconstruída mantém o id", 10L, doBanco.getId());

        try {
            new Leitura(codigo, Cenarios.INICIO, 4.2, 300, 18.3, 381, 72, 0.49);
            v.verificar("Temperatura de 300 °C deveria ser rejeitada", false);
        } catch (LeituraInvalidaException e) {
            v.verificar("Temperatura de 300 °C é rejeitada como falha de sensor", true);
            v.verificar("A causa original (ValorInvalidoException) é preservada",
                    e.getCause() instanceof ValorInvalidoException);
            System.out.println("           -> " + e.getMessage());
        }

        v.esperarExcecao("Leitura sem escada é rejeitada", LeituraInvalidaException.class,
                () -> new Leitura(null, Cenarios.INICIO, 4.2, 61.5, 18.3, 381, 72, 0.49));
        v.esperarExcecao("Leitura sem data é rejeitada", LeituraInvalidaException.class,
                () -> new Leitura(codigo, null, 4.2, 61.5, 18.3, 381, 72, 0.49));
        v.esperarExcecao("Leitura no futuro é rejeitada (relógio do ESP32 errado)", LeituraInvalidaException.class,
                () -> new Leitura(codigo, LocalDateTime.now().plusDays(1), 4.2, 61.5, 18.3, 381, 72, 0.49));

        return v.concluir();
    }
}
