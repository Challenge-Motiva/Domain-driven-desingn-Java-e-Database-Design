package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.excecao.LeituraInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.EnderecoMac;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidade: dispositivo IoT (ESP32) instalado em uma escada para enviar leituras.
 * No MVP, o ESP32 apenas reproduz o dataset via MQTT; ele é somente leitura e
 * nunca comanda a escada.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class DispositivoIoT {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private final EnderecoMac enderecoMac;
    private final CodigoEscada codigoEscada;
    private final String modelo;
    private String firmware;
    private LocalDateTime ultimaComunicacao;
    private boolean ativo;

    /**
     * Instalação de um dispositivo novo. Começa ativo e sem comunicação registrada.
     *
     * @param enderecoMac  endereço MAC do ESP32
     * @param codigoEscada escada onde o dispositivo foi instalado
     * @param modelo       modelo da placa (ex.: "ESP32-WROOM-32")
     * @param firmware     versão do firmware no formato 1.0.0
     * @throws ValorInvalidoException se faltar dado ou o firmware estiver fora do formato
     */
    public DispositivoIoT(EnderecoMac enderecoMac, CodigoEscada codigoEscada, String modelo, String firmware) {
        this(null, enderecoMac, codigoEscada, modelo, firmware, null, true);
    }

    /**
     * Reconstrução de um dispositivo já cadastrado.
     *
     * @param id                identificador no banco
     * @param enderecoMac       endereço MAC
     * @param codigoEscada      escada monitorada
     * @param modelo            modelo da placa
     * @param firmware          versão do firmware
     * @param ultimaComunicacao instante da última mensagem recebida, ou null
     * @param ativo             se o dispositivo está em operação
     * @throws ValorInvalidoException se faltar dado ou o firmware estiver fora do formato
     */
    public DispositivoIoT(Long id, EnderecoMac enderecoMac, CodigoEscada codigoEscada, String modelo,
                          String firmware, LocalDateTime ultimaComunicacao, boolean ativo) {
        if (id != null && id <= 0) {
            throw new ValorInvalidoException("O id do dispositivo deve ser positivo.");
        }
        if (enderecoMac == null || codigoEscada == null) {
            throw new ValorInvalidoException("Endereço MAC e escada são obrigatórios para o dispositivo.");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new ValorInvalidoException("O modelo do dispositivo é obrigatório.");
        }
        if (modelo.trim().length() > 40) {
            throw new ValorInvalidoException("O modelo do dispositivo deve ter no máximo 40 caracteres.");
        }
        this.id = id;
        this.enderecoMac = enderecoMac;
        this.codigoEscada = codigoEscada;
        this.modelo = modelo.trim();
        this.firmware = validarFirmware(firmware);
        this.ultimaComunicacao = ultimaComunicacao;
        this.ativo = ativo;
    }

    /**
     * Registra que o dispositivo enviou uma mensagem.
     *
     * @param quando instante da mensagem
     * @throws LeituraInvalidaException se o dispositivo estiver desativado ou a mensagem estiver fora de ordem
     */
    public void registrarComunicacao(LocalDateTime quando) {
        if (!ativo) {
            throw new LeituraInvalidaException("O dispositivo " + enderecoMac
                    + " está desativado e não pode enviar leituras.");
        }
        if (quando == null) {
            throw new LeituraInvalidaException("Informe o instante da comunicação.");
        }
        if (ultimaComunicacao != null && quando.isBefore(ultimaComunicacao)) {
            throw new LeituraInvalidaException("Comunicação fora de ordem do dispositivo " + enderecoMac + ".");
        }
        this.ultimaComunicacao = quando;
    }

    /**
     * @param agora      instante de referência
     * @param tolerancia tempo máximo aceitável sem comunicação
     * @return true se o dispositivo está ativo e se comunicou dentro da tolerância
     */
    public boolean isOnline(LocalDateTime agora, Duration tolerancia) {
        return ativo && ultimaComunicacao != null
                && Duration.between(ultimaComunicacao, agora).compareTo(tolerancia) <= 0;
    }

    /** Retira o dispositivo de operação (ex.: para troca ou manutenção). */
    public void desativar() {
        this.ativo = false;
    }

    /** Coloca o dispositivo de volta em operação. */
    public void reativar() {
        this.ativo = true;
    }

    /**
     * Mutador necessário: o firmware do ESP32 é atualizado remotamente (OTA).
     *
     * @param firmware nova versão no formato 1.0.0
     * @throws ValorInvalidoException se a versão estiver fora do formato
     */
    public void setFirmware(String firmware) {
        this.firmware = validarFirmware(firmware);
    }

    private static String validarFirmware(String versao) {
        if (versao == null || versao.trim().length() > 15 || !versao.trim().matches("\\d+\\.\\d+\\.\\d+")) {
            throw new ValorInvalidoException("Versão de firmware inválida: '" + versao + "'. Use o formato 1.0.0.");
        }
        return versao.trim();
    }

    /** @return id no banco, ou null se ainda não foi gravado */
    public Long getId() {
        return id;
    }

    /** @return endereço MAC */
    public EnderecoMac getEnderecoMac() {
        return enderecoMac;
    }

    /** @return escada monitorada */
    public CodigoEscada getCodigoEscada() {
        return codigoEscada;
    }

    /** @return modelo da placa */
    public String getModelo() {
        return modelo;
    }

    /** @return versão do firmware */
    public String getFirmware() {
        return firmware;
    }

    /** @return instante da última comunicação, ou null */
    public LocalDateTime getUltimaComunicacao() {
        return ultimaComunicacao;
    }

    /** @return true se o dispositivo está em operação */
    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public String toString() {
        return "DispositivoIoT{mac=" + enderecoMac
                + ", escada=" + codigoEscada
                + ", modelo='" + modelo + "'"
                + ", firmware=" + firmware
                + ", ativo=" + ativo
                + ", ultimaComunicacao=" + (ultimaComunicacao == null ? "nunca" : ultimaComunicacao.format(FORMATO))
                + "}";
    }
}
