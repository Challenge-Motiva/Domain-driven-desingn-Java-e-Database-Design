package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.DispositivoIoT;
import br.com.fiap.motiva.escadas.dominio.excecao.LeituraInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.EnderecoMac;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Driver de teste isolado da entidade {@link DispositivoIoT} (ESP32).
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteDispositivoIoT {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("DispositivoIoT");
        LocalDateTime t0 = Cenarios.INICIO;
        Duration tolerancia = Duration.ofMinutes(5);

        DispositivoIoT esp32 = new DispositivoIoT(new EnderecoMac("24:6F:28:AB:10:03"),
                new CodigoEscada("ECR-PAU-03"), "ESP32-WROOM-32", "1.0.0");
        System.out.println("           " + esp32);
        v.verificar("Dispositivo novo está ativo", esp32.isAtivo());
        v.verificar("Sem nenhuma comunicação, não está online", !esp32.isOnline(t0, tolerancia));

        esp32.registrarComunicacao(t0);
        v.verificar("Logo após comunicar, está online", esp32.isOnline(t0.plusMinutes(2), tolerancia));
        v.verificar("10 min sem comunicar, fica offline", !esp32.isOnline(t0.plusMinutes(10), tolerancia));
        v.esperarExcecao("Comunicação fora de ordem é rejeitada", LeituraInvalidaException.class,
                () -> esp32.registrarComunicacao(t0.minusMinutes(1)));

        esp32.desativar();
        v.esperarExcecao("Dispositivo desativado não envia leituras", LeituraInvalidaException.class,
                () -> esp32.registrarComunicacao(t0.plusMinutes(1)));
        esp32.reativar();
        esp32.registrarComunicacao(t0.plusMinutes(1));
        v.verificar("Dispositivo reativado volta a comunicar", esp32.isOnline(t0.plusMinutes(1), tolerancia));

        esp32.setFirmware("1.1.0");
        v.verificarIgual("Firmware atualizado", "1.1.0", esp32.getFirmware());
        v.esperarExcecao("Firmware fora do formato é rejeitado", ValorInvalidoException.class,
                () -> esp32.setFirmware("v2"));
        v.esperarExcecao("Dispositivo sem modelo é rejeitado", ValorInvalidoException.class,
                () -> new DispositivoIoT(new EnderecoMac("24:6F:28:AB:10:04"), new CodigoEscada("ECR-PAU-04"),
                        "", "1.0.0"));

        return v.concluir();
    }
}
