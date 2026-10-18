package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.EnderecoMac;
import br.com.fiap.motiva.escadas.dominio.vo.Justificativa;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;
import br.com.fiap.motiva.escadas.dominio.vo.SiglaEstacao;

/**
 * Driver de teste isolado dos Value Objects: normalização, validação e igualdade por valor.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteValueObjects {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("Value Objects");

        // SiglaEstacao
        SiglaEstacao sigla = new SiglaEstacao(" pau ");
        v.verificarIgual("SiglaEstacao normaliza espaços e minúsculas", "PAU", sigla.getValor());
        v.verificar("SiglaEstacao é igual por valor", sigla.equals(new SiglaEstacao("PAU")));
        v.esperarExcecao("SiglaEstacao rejeita 2 letras", ValorInvalidoException.class, () -> new SiglaEstacao("PA"));
        v.esperarExcecao("SiglaEstacao rejeita número", ValorInvalidoException.class, () -> new SiglaEstacao("PA1"));
        v.esperarExcecao("SiglaEstacao rejeita nulo", ValorInvalidoException.class, () -> new SiglaEstacao(null));

        // CodigoEscada
        CodigoEscada lido = new CodigoEscada("ecr-pau-03");
        CodigoEscada gerado = new CodigoEscada(sigla, 3);
        v.verificarIgual("CodigoEscada normaliza para maiúsculas", "ECR-PAU-03", lido.getValor());
        v.verificar("CodigoEscada lido e gerado pela sigla são iguais", lido.equals(gerado));
        v.verificarIgual("CodigoEscada informa a sigla da estação", sigla, lido.getSiglaEstacao());
        v.esperarExcecao("CodigoEscada rejeita prefixo errado", ValorInvalidoException.class,
                () -> new CodigoEscada("ESC-PAU-03"));
        v.esperarExcecao("CodigoEscada rejeita número 00", ValorInvalidoException.class,
                () -> new CodigoEscada("ECR-PAU-00"));
        v.esperarExcecao("CodigoEscada rejeita número 100", ValorInvalidoException.class,
                () -> new CodigoEscada(sigla, 100));

        // Medicao
        Medicao medicao = new Medicao(TipoSinal.VIBRACAO, 4.2);
        v.verificarIgual("Medicao formata com vírgula e unidade", "4,20 mm/s", medicao.formatada());
        v.verificar("Medicao é igual por tipo e valor", medicao.equals(new Medicao(TipoSinal.VIBRACAO, 4.2)));
        v.verificar("Medicoes de sinais diferentes não são iguais",
                !medicao.equals(new Medicao(TipoSinal.TEMPERATURA, 4.2)));
        v.esperarExcecao("Medicao rejeita vibração negativa", ValorInvalidoException.class,
                () -> new Medicao(TipoSinal.VIBRACAO, -1));
        v.esperarExcecao("Medicao rejeita nível de óleo acima de 100%", ValorInvalidoException.class,
                () -> new Medicao(TipoSinal.NIVEL_OLEO, 120));
        v.esperarExcecao("Medicao rejeita valor não numérico", ValorInvalidoException.class,
                () -> new Medicao(TipoSinal.TEMPERATURA, Double.NaN));

        // EnderecoMac
        EnderecoMac mac = new EnderecoMac("24-6f-28-ab-10-01");
        v.verificarIgual("EnderecoMac normaliza separador e maiúsculas", "24:6F:28:AB:10:01", mac.getValor());
        v.esperarExcecao("EnderecoMac rejeita caractere não hexadecimal", ValorInvalidoException.class,
                () -> new EnderecoMac("ZZ:6F:28:AB:10:01"));
        v.esperarExcecao("EnderecoMac rejeita tamanho errado", ValorInvalidoException.class,
                () -> new EnderecoMac("24:6F:28:AB:10"));

        // Justificativa
        Justificativa justificativa = new Justificativa("  Vibração confirmada em campo.  ");
        v.verificarIgual("Justificativa remove espaços das pontas", "Vibração confirmada em campo.",
                justificativa.getTexto());
        v.esperarExcecao("Justificativa rejeita texto curto", ValorInvalidoException.class,
                () -> new Justificativa("ok"));
        v.esperarExcecao("Justificativa rejeita texto acima de 500 caracteres", ValorInvalidoException.class,
                () -> new Justificativa("x".repeat(501)));

        return v.concluir();
    }
}
