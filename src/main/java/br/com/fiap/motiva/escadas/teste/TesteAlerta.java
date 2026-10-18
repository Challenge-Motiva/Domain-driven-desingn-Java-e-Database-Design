package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.Alerta;
import br.com.fiap.motiva.escadas.dominio.entidade.EscadaRolante;
import br.com.fiap.motiva.escadas.dominio.entidade.Leitura;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.StatusAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.TipoSinal;
import br.com.fiap.motiva.escadas.dominio.excecao.TransicaoDeStatusInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.Justificativa;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Driver de teste isolado da entidade {@link Alerta}: evidência, decisão do gestor
 * e transições de status bloqueadas.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteAlerta {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("Alerta");
        EscadaRolante escada = Cenarios.escada(Cenarios.paulista(), 3, GrupoManutencao.CRITICA_A);
        Justificativa motivo = new Justificativa("Vibração crítica confirmada pelo histórico.");

        Alerta alerta = Cenarios.alertaCriticoAberto(escada);
        LocalDateTime abertura = alerta.getAbertoEm();
        System.out.println("           " + alerta);
        v.verificarIgual("Alerta nasce aberto", StatusAlerta.ABERTO, alerta.getStatus());
        v.verificarIgual("Alerta guarda a evidência", "Vibração 7,40 mm/s (limite 7,10 mm/s)",
                alerta.descreverEvidencia());
        v.verificarIgual("Alerta aponta a escada de origem", escada.getCodigo(), alerta.getCodigoEscada());
        v.verificarIgual("Tempo em aberto é contado até agora", Duration.ofMinutes(30),
                alerta.calcularTempoEmAberto(abertura.plusMinutes(30)));

        // Decisão inválida não altera nada
        v.esperarExcecao("Confirmar sem justificativa é bloqueado", ValorInvalidoException.class,
                () -> alerta.confirmar("Roberto Almeida", null, abertura.plusMinutes(10)));
        v.esperarExcecao("Confirmar sem gestor é bloqueado", ValorInvalidoException.class,
                () -> alerta.confirmar(" ", motivo, abertura.plusMinutes(10)));
        v.esperarExcecao("Decidir antes da abertura é bloqueado", ValorInvalidoException.class,
                () -> alerta.confirmar("Roberto Almeida", motivo, abertura.minusMinutes(1)));
        v.verificarIgual("Após tentativas inválidas, continua aberto", StatusAlerta.ABERTO, alerta.getStatus());

        // Decisão válida
        alerta.confirmar("Roberto Almeida", motivo, abertura.plusMinutes(15));
        System.out.println("           " + alerta);
        v.verificar("Alerta confirmado", alerta.isConfirmado());
        v.verificarIgual("Gestor registrado", "Roberto Almeida", alerta.getDecididoPor());
        v.verificarIgual("Tempo em aberto para na decisão", Duration.ofMinutes(15),
                alerta.calcularTempoEmAberto(abertura.plusHours(5)));
        v.esperarExcecao("Alerta confirmado não pode ser confirmado de novo",
                TransicaoDeStatusInvalidaException.class,
                () -> alerta.confirmar("Fernanda", motivo, abertura.plusMinutes(20)));
        v.esperarExcecao("Alerta confirmado não pode ser descartado",
                TransicaoDeStatusInvalidaException.class,
                () -> alerta.descartar("Fernanda", motivo, abertura.plusMinutes(20)));

        // Descarte
        Leitura outra = Cenarios.leituraComVibracao(escada.getCodigo(), abertura.plusHours(1), 5.0);
        Alerta deAtencao = escada.registrarLeitura(outra, Cenarios.POLITICA).get(0);
        deAtencao.descartar("Fernanda", new Justificativa("Pico isolado após partida, normalizado."),
                outra.getDataHora().plusMinutes(5));
        v.verificarIgual("Alerta de atenção descartado", StatusAlerta.DESCARTADO, deAtencao.getStatus());

        // Construção inválida
        v.esperarExcecao("Medição que não é da leitura não abre alerta", ValorInvalidoException.class,
                () -> new Alerta(outra, new Medicao(TipoSinal.VIBRACAO, 9.9), NivelAlerta.CRITICO, 7.1));
        v.esperarExcecao("Alerta do banco com status CONFIRMADO exige dados da decisão", ValorInvalidoException.class,
                () -> new Alerta(5L, outra, TipoSinal.VIBRACAO, NivelAlerta.ATENCAO, 4.5,
                        StatusAlerta.CONFIRMADO, null, null, null));

        Alerta doBanco = new Alerta(5L, outra, TipoSinal.VIBRACAO, NivelAlerta.ATENCAO, 4.5,
                StatusAlerta.ABERTO, null, null, null);
        v.verificarIgual("Alerta reconstruído do banco mantém o id", 5L, doBanco.getId());

        return v.concluir();
    }
}
