package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.EscadaRolante;
import br.com.fiap.motiva.escadas.dominio.entidade.Estacao;
import br.com.fiap.motiva.escadas.dominio.enums.Fabricante;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.Linha;
import br.com.fiap.motiva.escadas.dominio.enums.StatusCondicao;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.SiglaEstacao;

import java.util.List;
import java.util.Map;

/**
 * Driver de teste isolado da entidade {@link Estacao}.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteEstacao {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("Estacao");

        Estacao nova = new Estacao("Butantã", new SiglaEstacao("BUT"), Linha.LINHA_4_AMARELA);
        System.out.println("           " + nova);
        v.verificar("Estação nova ainda não tem id", nova.getId() == null);
        v.verificarIgual("Linha é exibida pelo nome", "Linha 4-Amarela", nova.getLinha().toString());
        v.esperarExcecao("Estação sem nome é rejeitada", ValorInvalidoException.class,
                () -> new Estacao(" ", new SiglaEstacao("BUT"), Linha.LINHA_4_AMARELA));
        v.esperarExcecao("Estação sem linha é rejeitada", ValorInvalidoException.class,
                () -> new Estacao("Butantã", new SiglaEstacao("BUT"), null));

        Estacao paulista = Cenarios.paulista();
        Estacao pinheiros = Cenarios.pinheiros();
        EscadaRolante critica = Cenarios.escada(paulista, 1, GrupoManutencao.CRITICA_A);
        EscadaRolante naoCritica = Cenarios.escada(paulista, 2, GrupoManutencao.NAO_CRITICA_A);
        v.verificarIgual("Paulista tem 2 escadas", 2, paulista.getEscadas().size());

        v.esperarExcecao("Mesma escada não pode ser cadastrada duas vezes", ValorInvalidoException.class,
                () -> paulista.adicionarEscada(critica));
        EscadaRolante dePinheiros = new EscadaRolante(new CodigoEscada("ECR-PIN-01"), pinheiros,
                GrupoManutencao.CRITICA_B, Fabricante.OTIS, "Rua → mezanino");
        v.esperarExcecao("Escada de Pinheiros não entra em Paulista", ValorInvalidoException.class,
                () -> paulista.adicionarEscada(dePinheiros));
        v.esperarExcecao("Lista de escadas não pode ser alterada por fora", UnsupportedOperationException.class,
                () -> paulista.getEscadas().clear());

        Map<StatusCondicao, Long> antes = paulista.contarEscadasPorStatus();
        v.verificarIgual("Antes das leituras, as 2 escadas estão sem dados", 2L, antes.get(StatusCondicao.SEM_DADOS));
        v.verificarIgual("A contagem mostra todos os status, mesmo zerados", 4, antes.size());

        // As duas escadas recebem vibração crítica: a crítica deve vir primeiro no ranking
        critica.registrarLeitura(Cenarios.leituraComVibracao(critica.getCodigo(), Cenarios.INICIO, 7.5),
                Cenarios.POLITICA);
        naoCritica.registrarLeitura(Cenarios.leituraComVibracao(naoCritica.getCodigo(), Cenarios.INICIO, 7.5),
                Cenarios.POLITICA);
        List<EscadaRolante> ranking = paulista.listarEscadasQueExigemAtencao();
        v.verificarIgual("Duas escadas exigem atenção", 2, ranking.size());
        v.verificarIgual("No mesmo status, a escada crítica vem primeiro (RN01)", critica, ranking.get(0));
        v.verificarIgual("Contagem por status atualizada", 2L,
                paulista.contarEscadasPorStatus().get(StatusCondicao.INTERVENCAO_RECOMENDADA));

        return v.concluir();
    }
}
