package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.Alerta;
import br.com.fiap.motiva.escadas.dominio.entidade.EscadaRolante;
import br.com.fiap.motiva.escadas.dominio.entidade.Estacao;
import br.com.fiap.motiva.escadas.dominio.entidade.Leitura;
import br.com.fiap.motiva.escadas.dominio.enums.Fabricante;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.StatusCondicao;
import br.com.fiap.motiva.escadas.dominio.excecao.LeituraInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Driver de teste isolado da entidade {@link EscadaRolante}: avaliação de leituras,
 * geração de alertas, inatividade e prioridade.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteEscadaRolante {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("EscadaRolante");
        Estacao paulista = Cenarios.paulista();
        LocalDateTime t0 = Cenarios.INICIO;

        EscadaRolante escada = Cenarios.escada(paulista, 3, GrupoManutencao.CRITICA_A);
        System.out.println("           " + escada);
        v.verificarIgual("Escada nova começa sem dados (cinza)", StatusCondicao.SEM_DADOS, escada.getStatus());
        v.verificar("Escada nova não tem última leitura", escada.getUltimaLeitura() == null);

        v.esperarExcecao("Código de Pinheiros não pode ser instalado em Paulista", ValorInvalidoException.class,
                () -> new EscadaRolante(new CodigoEscada("ECR-PIN-01"), paulista, GrupoManutencao.CRITICA_A,
                        Fabricante.OTIS, "Plataforma → mezanino"));
        v.esperarExcecao("Escada reconstruída sem leitura não pode estar verde", ValorInvalidoException.class,
                () -> new EscadaRolante(9L, new CodigoEscada("ECR-PAU-09"), paulista, GrupoManutencao.CRITICA_A,
                        Fabricante.OTIS, "Plataforma → mezanino", StatusCondicao.NORMAL, null));

        // Leitura normal
        List<Alerta> alertas = escada.registrarLeitura(Cenarios.leituraNormal(escada.getCodigo(), t0), Cenarios.POLITICA);
        v.verificar("Leitura normal não gera alerta", alertas.isEmpty());
        v.verificarIgual("Leitura normal deixa a escada verde", StatusCondicao.NORMAL, escada.getStatus());

        // Vibração em atenção
        alertas = escada.registrarLeitura(
                Cenarios.leituraComVibracao(escada.getCodigo(), t0.plusMinutes(1), 5.2), Cenarios.POLITICA);
        v.verificarIgual("Vibração 5,2 mm/s gera 1 alerta", 1, alertas.size());
        v.verificarIgual("O alerta é de atenção", NivelAlerta.ATENCAO, alertas.get(0).getNivel());
        v.verificarIgual("A escada fica amarela", StatusCondicao.ATENCAO, escada.getStatus());

        // Dois sinais críticos na mesma leitura
        Leitura critica = new Leitura(escada.getCodigo(), t0.plusMinutes(2), 7.4, 88, 18, 380, 75, 0.50);
        alertas = escada.registrarLeitura(critica, Cenarios.POLITICA);
        v.verificarIgual("Vibração e temperatura críticas geram 2 alertas", 2, alertas.size());
        v.verificarIgual("A escada fica vermelha (pior sinal)", StatusCondicao.INTERVENCAO_RECOMENDADA,
                escada.getStatus());
        alertas.forEach(a -> System.out.println("           " + a.descreverEvidencia() + " -> " + a.getNivel()));

        // Leituras que devem ser rejeitadas
        v.esperarExcecao("Leitura de outra escada é rejeitada", LeituraInvalidaException.class,
                () -> escada.registrarLeitura(
                        Cenarios.leituraNormal(new CodigoEscada("ECR-PAU-01"), t0.plusMinutes(3)), Cenarios.POLITICA));
        v.esperarExcecao("Leitura fora de ordem é rejeitada", LeituraInvalidaException.class,
                () -> escada.registrarLeitura(
                        Cenarios.leituraNormal(escada.getCodigo(), t0.plusMinutes(1)), Cenarios.POLITICA));
        v.verificarIgual("Leitura rejeitada não altera o status", StatusCondicao.INTERVENCAO_RECOMENDADA,
                escada.getStatus());

        // Inatividade
        Duration tolerancia = Duration.ofMinutes(15);
        v.verificar("10 min sem leitura ainda está dentro da tolerância",
                !escada.verificarInatividade(t0.plusMinutes(12), tolerancia));
        v.verificar("30 min sem leitura deixa a escada cinza",
                escada.verificarInatividade(t0.plusMinutes(32), tolerancia));
        v.verificarIgual("Status após inatividade", StatusCondicao.SEM_DADOS, escada.getStatus());
        v.verificarIgual("Tempo desde a última leitura", Duration.ofMinutes(30),
                escada.calcularTempoDesdeUltimaLeitura(t0.plusMinutes(32)).orElseThrow());

        // Prioridade (RN01)
        EscadaRolante criticaVermelha = Cenarios.escada(paulista, 4, GrupoManutencao.CRITICA_B);
        EscadaRolante naoCriticaVermelha = Cenarios.escada(paulista, 5, GrupoManutencao.NAO_CRITICA_A);
        EscadaRolante criticaAmarela = Cenarios.escada(paulista, 6, GrupoManutencao.CRITICA_A);
        criticaVermelha.registrarLeitura(Cenarios.leituraComVibracao(criticaVermelha.getCodigo(), t0, 7.5), Cenarios.POLITICA);
        naoCriticaVermelha.registrarLeitura(Cenarios.leituraComVibracao(naoCriticaVermelha.getCodigo(), t0, 7.5), Cenarios.POLITICA);
        criticaAmarela.registrarLeitura(Cenarios.leituraComVibracao(criticaAmarela.getCodigo(), t0, 5.0), Cenarios.POLITICA);
        v.verificar("Crítica vermelha > não crítica vermelha",
                criticaVermelha.calcularPrioridade() > naoCriticaVermelha.calcularPrioridade());
        v.verificar("Não crítica vermelha > crítica amarela (gravidade pesa mais que o grupo)",
                naoCriticaVermelha.calcularPrioridade() > criticaAmarela.calcularPrioridade());

        return v.concluir();
    }
}
