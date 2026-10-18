package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.Alerta;
import br.com.fiap.motiva.escadas.dominio.entidade.EscadaRolante;
import br.com.fiap.motiva.escadas.dominio.entidade.Equipe;
import br.com.fiap.motiva.escadas.dominio.entidade.Estacao;
import br.com.fiap.motiva.escadas.dominio.entidade.OrdemServico;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.StatusOrdemServico;
import br.com.fiap.motiva.escadas.dominio.excecao.AcionamentoNaoAutorizadoException;
import br.com.fiap.motiva.escadas.dominio.excecao.EquipeIndisponivelException;
import br.com.fiap.motiva.escadas.dominio.excecao.TransicaoDeStatusInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.Justificativa;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Driver de teste isolado da entidade {@link OrdemServico}: bloqueio de acionamento
 * sem gestor, alocação de equipe e ciclo de vida da OS.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteOrdemServico {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("OrdemServico");
        Estacao paulista = Cenarios.paulista();
        Equipe equipe = Cenarios.equipeNoite(paulista);

        // RN02: sem confirmação do gestor, nenhuma equipe é acionada
        Alerta aberto = Cenarios.alertaCriticoAberto(Cenarios.escada(paulista, 1, GrupoManutencao.CRITICA_A));
        v.esperarExcecao("OS para alerta ABERTO é bloqueada (RN02)", AcionamentoNaoAutorizadoException.class,
                () -> new OrdemServico(aberto, equipe, aberto.getAbertoEm().plusMinutes(5)));
        aberto.descartar("Fernanda", new Justificativa("Falso positivo: sensor recalibrado."),
                aberto.getAbertoEm().plusMinutes(5));
        v.esperarExcecao("OS para alerta DESCARTADO é bloqueada", AcionamentoNaoAutorizadoException.class,
                () -> new OrdemServico(aberto, equipe, aberto.getAbertoEm().plusMinutes(10)));
        v.verificar("Tentativas bloqueadas não ocupam a equipe", equipe.isDisponivel());

        // Abertura válida
        Alerta confirmado = Cenarios.alertaCriticoConfirmado(Cenarios.escada(paulista, 3, GrupoManutencao.CRITICA_A));
        LocalDateTime abertura = confirmado.getDecididoEm().plusMinutes(5);
        v.esperarExcecao("OS não pode ser aberta antes da confirmação", ValorInvalidoException.class,
                () -> new OrdemServico(confirmado, equipe, confirmado.getDecididoEm().minusMinutes(1)));
        OrdemServico os = new OrdemServico(confirmado, equipe, abertura);
        System.out.println("           " + os);
        v.verificarIgual("OS nasce ABERTA", StatusOrdemServico.ABERTA, os.getStatus());
        v.verificar("Equipe fica alocada", !equipe.isDisponivel());

        Alerta outroConfirmado = Cenarios.alertaCriticoConfirmado(Cenarios.escada(paulista, 4, GrupoManutencao.CRITICA_B));
        v.esperarExcecao("Equipe ocupada não pode receber outra OS", EquipeIndisponivelException.class,
                () -> new OrdemServico(outroConfirmado, equipe, abertura.plusMinutes(1)));

        // Ciclo de vida
        v.esperarExcecao("OS não pode ser concluída antes de iniciar", TransicaoDeStatusInvalidaException.class,
                () -> os.concluir(abertura.plusHours(1), "Rolamento substituído."));
        os.iniciarAtendimento(abertura.plusMinutes(40));
        v.verificarIgual("Atendimento iniciado", StatusOrdemServico.EM_ATENDIMENTO, os.getStatus());
        v.esperarExcecao("Diagnóstico curto é rejeitado", ValorInvalidoException.class,
                () -> os.concluir(abertura.plusHours(2), "feito"));
        v.verificarIgual("Diagnóstico rejeitado não conclui a OS", StatusOrdemServico.EM_ATENDIMENTO, os.getStatus());
        os.concluir(abertura.plusHours(2), "Rolamento do redutor substituído e eixo realinhado.");
        System.out.println("           " + os);
        v.verificarIgual("OS concluída", StatusOrdemServico.CONCLUIDA, os.getStatus());
        v.verificar("Equipe liberada após a conclusão", equipe.isDisponivel());
        v.verificarIgual("Tempo de reparo (base do MTTR)", Duration.ofHours(2), os.calcularTempoDeReparo().orElseThrow());
        v.esperarExcecao("OS concluída não pode ser cancelada", TransicaoDeStatusInvalidaException.class,
                () -> os.cancelar(abertura.plusHours(3), "Cancelamento indevido"));

        // Cancelamento
        OrdemServico cancelada = new OrdemServico(outroConfirmado, equipe, abertura.plusHours(3));
        cancelada.cancelar(abertura.plusHours(4), "Escada interditada pela operação para obra civil.");
        v.verificarIgual("OS cancelada", StatusOrdemServico.CANCELADA, cancelada.getStatus());
        v.verificar("Equipe liberada após o cancelamento", equipe.isDisponivel());
        v.verificar("OS cancelada não tem tempo de reparo", cancelada.calcularTempoDeReparo().isEmpty());

        return v.concluir();
    }
}
