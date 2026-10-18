package br.com.fiap.motiva.escadas;

import br.com.fiap.motiva.escadas.dominio.entidade.Alerta;
import br.com.fiap.motiva.escadas.dominio.entidade.DispositivoIoT;
import br.com.fiap.motiva.escadas.dominio.entidade.EscadaRolante;
import br.com.fiap.motiva.escadas.dominio.entidade.Equipe;
import br.com.fiap.motiva.escadas.dominio.entidade.Estacao;
import br.com.fiap.motiva.escadas.dominio.entidade.Leitura;
import br.com.fiap.motiva.escadas.dominio.entidade.OrdemServico;
import br.com.fiap.motiva.escadas.dominio.enums.Fabricante;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.Linha;
import br.com.fiap.motiva.escadas.dominio.enums.StatusCondicao;
import br.com.fiap.motiva.escadas.dominio.enums.Turno;
import br.com.fiap.motiva.escadas.dominio.excecao.DominioException;
import br.com.fiap.motiva.escadas.dominio.regra.PoliticaDeLimites;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.EnderecoMac;
import br.com.fiap.motiva.escadas.dominio.vo.Justificativa;
import br.com.fiap.motiva.escadas.dominio.vo.SiglaEstacao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe driver principal. Conta, em forma de STORYBOARD, um turno de monitoramento
 * de ponta a ponta: da chegada das leituras até a equipe concluir o reparo.
 * <p>
 * Cada "CENA" abaixo é um passo do fluxo lógico do sistema. O mesmo fluxo aparece
 * como fluxograma (System Design) no README.
 * <p>
 * Todos os dados são fictícios. Os limites de alerta são provisórios até a análise
 * exploratória do dataset da Motiva.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class Main {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {

        // =====================================================================
        // CENA 1 — A rede é cadastrada
        // A Motiva tem estações, cada estação tem escadas, e cada escada
        // pertence a um dos 4 grupos de manutenção. O código ECR-XXX-00 é
        // validado contra a sigla da estação: ECR-PAU-03 só pode estar na Paulista.
        // =====================================================================
        cena(1, "Cadastro da rede");
        Estacao paulista = new Estacao(1L, "Paulista", new SiglaEstacao("PAU"), Linha.LINHA_4_AMARELA);
        Estacao pinheiros = new Estacao(2L, "Pinheiros", new SiglaEstacao("PIN"), Linha.LINHA_4_AMARELA);
        Estacao luz = new Estacao(3L, "Luz", new SiglaEstacao("LUZ"), Linha.LINHA_4_AMARELA);

        EscadaRolante pau01 = cadastrar(paulista, 1, GrupoManutencao.CRITICA_A, Fabricante.OTIS, "Plataforma → mezanino");
        EscadaRolante pau03 = cadastrar(paulista, 3, GrupoManutencao.CRITICA_A, Fabricante.KONE, "Mezanino → rua (Av. Paulista)");
        EscadaRolante pin02 = cadastrar(pinheiros, 2, GrupoManutencao.NAO_CRITICA_A, Fabricante.SCHINDLER, "Mezanino → bilheteria");
        EscadaRolante luz01 = cadastrar(luz, 1, GrupoManutencao.CRITICA_B, Fabricante.TK_ELEVATOR, "Plataforma → transferência CPTM");
        List<EscadaRolante> frota = List.of(pau01, pau03, pin02, luz01);

        List.of(paulista, pinheiros, luz).forEach(Main::imprimir);
        frota.forEach(Main::imprimir);

        // =====================================================================
        // CENA 2 — Um ESP32 é instalado em cada escada
        // O dispositivo só LÊ e envia dados; nunca comanda a escada.
        // No MVP ele reproduz as linhas do dataset da Motiva via MQTT.
        // =====================================================================
        cena(2, "Instalação dos dispositivos IoT");
        List<DispositivoIoT> dispositivos = new ArrayList<>();
        int sufixo = 1;
        for (EscadaRolante escada : frota) {
            String mac = String.format("24:6F:28:AB:10:%02X", sufixo++);
            dispositivos.add(new DispositivoIoT(new EnderecoMac(mac), escada.getCodigo(), "ESP32-WROOM-32", "1.0.0"));
        }
        dispositivos.forEach(Main::imprimir);

        // =====================================================================
        // CENA 3 — O motor de regras é carregado
        // Uma regra por sinal. Cada tipo de regra (limite superior, inferior
        // ou faixa nominal) avalia de um jeito: é o polimorfismo do domínio.
        // =====================================================================
        cena(3, "Política de limites (PROVISÓRIA)");
        PoliticaDeLimites politica = PoliticaDeLimites.provisoria();
        imprimir(politica);

        // =====================================================================
        // CENA 4 — Primeiras leituras: tudo normal
        // Cada leitura atualiza o semáforo da escada. Sem alerta, fica verde.
        // =====================================================================
        cena(4, "Primeira rodada de leituras (06:00)");
        LocalDateTime t = LocalDateTime.of(2026, 10, 15, 6, 0);
        for (int i = 0; i < frota.size(); i++) {
            EscadaRolante escada = frota.get(i);
            receber(escada, dispositivos.get(i), new Leitura(escada.getCodigo(), t, 2.1, 55, 18, 380, 75, 0.50), politica);
        }

        // =====================================================================
        // CENA 5 — A ECR-PAU-03 começa a degradar
        // A vibração sobe e passa do limite de atenção (amarelo). Uma hora
        // depois, vibração e temperatura passam do crítico (vermelho).
        // Ao mesmo tempo, o óleo da ECR-PIN-02 baixa (atenção).
        // =====================================================================
        cena(5, "Degradação detectada");
        List<Alerta> alertasDoTurno = new ArrayList<>();
        alertasDoTurno.addAll(receber(pau03, dispositivos.get(1),
                new Leitura(pau03.getCodigo(), t.plusHours(1), 5.2, 63, 19, 381, 74, 0.50), politica));
        alertasDoTurno.addAll(receber(pau03, dispositivos.get(1),
                new Leitura(pau03.getCodigo(), t.plusHours(2), 7.6, 87, 21, 379, 73, 0.49), politica));
        alertasDoTurno.addAll(receber(pin02, dispositivos.get(2),
                new Leitura(pin02.getCodigo(), t.plusHours(2), 2.3, 56, 18, 380, 36, 0.50), politica));
        receber(pau01, dispositivos.get(0),
                new Leitura(pau01.getCodigo(), t.plusHours(2), 2.0, 54, 18, 380, 76, 0.50), politica);

        // =====================================================================
        // CENA 6 — A ECR-LUZ-01 para de enviar dados
        // Sensor offline não pode parecer "verde": a escada vira cinza.
        // Uma leitura com valor impossível também é rejeitada (falha de sensor).
        // =====================================================================
        cena(6, "Escada sem dados e leitura impossível");
        LocalDateTime agora = t.plusHours(2).plusMinutes(10);
        for (EscadaRolante escada : frota) {
            if (escada.verificarInatividade(agora, Duration.ofMinutes(30))) {
                System.out.println("  " + escada.getCodigo() + " está sem dados há "
                        + escada.calcularTempoDesdeUltimaLeitura(agora).map(Duration::toMinutes).orElse(0L) + " min.");
            }
        }
        tentar("Leitura com 999 °C enviada pela ECR-LUZ-01",
                () -> new Leitura(luz01.getCodigo(), agora, 2.0, 999, 18, 380, 75, 0.50));

        // =====================================================================
        // CENA 7 — O gestor abre o dashboard
        // Cada estação mostra a contagem por cor; o ranking mostra quem
        // atender primeiro (gravidade, depois criticidade do grupo — RN01).
        // =====================================================================
        cena(7, "Dashboard do gestor");
        for (Estacao estacao : List.of(paulista, pinheiros, luz)) {
            Map<StatusCondicao, Long> contagem = estacao.contarEscadasPorStatus();
            String resumo = contagem.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(e -> e.getKey().getCor() + ": " + e.getValue())
                    .collect(Collectors.joining(" | "));
            System.out.println("  " + estacao.getNome() + " → " + resumo);
        }
        System.out.println("  Ranking de prioridade:");
        frota.stream()
                .sorted(Comparator.comparingInt(EscadaRolante::calcularPrioridade).reversed())
                .forEach(e -> System.out.println("    " + e.calcularPrioridade() + " pts  " + e.getCodigo()
                        + "  " + e.getStatus() + "  [" + e.getGrupo() + "]"));
        System.out.println("  Fila de alertas abertos:");
        alertasDoTurno.forEach(a -> System.out.println("    " + a.getCodigoEscada() + "  " + a.getNivel()
                + "  " + a.descreverEvidencia() + "  (aberto há " + a.calcularTempoEmAberto(agora).toMinutes() + " min)"));

        // =====================================================================
        // CENA 8 — O gestor decide, alerta por alerta
        // Nada acontece sem justificativa. Ele confirma a vibração crítica,
        // descarta a vibração de atenção (superada pelo crítico) e a temperatura
        // (mesma causa, mesma OS) e deixa o óleo da ECR-PIN-02 para a
        // preventiva já programada.
        // =====================================================================
        cena(8, "Decisão do gestor");
        Alerta vibracaoAtencao = buscar(alertasDoTurno, pau03, "VIBRACAO", "ATENCAO");
        Alerta vibracaoCritica = buscar(alertasDoTurno, pau03, "VIBRACAO", "CRITICO");
        Alerta temperaturaCritica = buscar(alertasDoTurno, pau03, "TEMPERATURA", "CRITICO");
        Alerta oleoPinheiros = buscar(alertasDoTurno, pin02, "NIVEL_OLEO", "ATENCAO");
        LocalDateTime decisao = agora.plusMinutes(5);

        tentar("Confirmar sem justificativa",
                () -> vibracaoCritica.confirmar("Roberto Almeida", null, decisao));
        vibracaoCritica.confirmar("Roberto Almeida",
                new Justificativa("Vibração subiu de 2,1 para 7,6 mm/s em 2 horas; escada crítica do grupo A."), decisao);
        vibracaoAtencao.descartar("Roberto Almeida",
                new Justificativa("Superado pelo alerta crítico de vibração da mesma escada."), decisao);
        temperaturaCritica.descartar("Roberto Almeida",
                new Justificativa("Aquecimento é consequência do mesmo atrito; tratado na OS da vibração."), decisao);
        oleoPinheiros.descartar("Roberto Almeida",
                new Justificativa("Reposição de óleo já prevista na preventiva P1 desta madrugada."), decisao);
        alertasDoTurno.forEach(Main::imprimir);

        // =====================================================================
        // CENA 9 — Acionamento da equipe
        // RN02: o sistema bloqueia a OS para um alerta que o gestor não confirmou.
        // Para o alerta confirmado, a equipe é alocada e fica indisponível.
        // =====================================================================
        cena(9, "Acionamento da equipe");
        Equipe equipeNoturna = new Equipe(1L, "Equipe Noturna Paulista", Turno.NOITE, paulista, 3, true);
        Equipe equipeManha = new Equipe(2L, "Equipe Manhã Linha 4", Turno.MANHA, paulista, 4, true);
        imprimir(equipeNoturna);
        imprimir(equipeManha);

        tentar("Abrir OS para o alerta de temperatura (descartado)",
                () -> new OrdemServico(temperaturaCritica, equipeManha, decisao.plusMinutes(1)));
        Equipe escolhida = equipeManha.atuaNoHorario(decisao.toLocalTime()) ? equipeManha : equipeNoturna;
        OrdemServico os = new OrdemServico(vibracaoCritica, escolhida, decisao.plusMinutes(2));
        imprimir(os);
        tentar("Acionar a mesma equipe de novo antes de concluir a OS",
                () -> new OrdemServico(vibracaoCritica, escolhida, decisao.plusMinutes(3)));

        // =====================================================================
        // CENA 10 — O técnico atende e fecha a OS
        // O tempo entre abertura e conclusão alimenta o MTTR do dashboard.
        // =====================================================================
        cena(10, "Atendimento em campo");
        tentar("Concluir antes de iniciar o atendimento",
                () -> os.concluir(decisao.plusHours(1), "Rolamento substituído."));
        os.iniciarAtendimento(decisao.plusMinutes(35));
        os.concluir(decisao.plusMinutes(35).plusHours(1).plusMinutes(20),
                "Rolamento do redutor com desgaste; substituído e eixo realinhado.");
        imprimir(os);
        System.out.println("  Tempo de reparo: " + os.calcularTempoDeReparo().map(Duration::toMinutes).orElse(0L) + " min");
        imprimir(escolhida);

        // =====================================================================
        // CENA 11 — A escada volta ao normal
        // A próxima leitura após o reparo deixa a ECR-PAU-03 verde de novo.
        // =====================================================================
        cena(11, "Escada de volta à operação normal");
        receber(pau03, dispositivos.get(1),
                new Leitura(pau03.getCodigo(), os.getEncerradaEm().plusMinutes(10), 2.2, 56, 18, 380, 73, 0.50), politica);
        imprimir(pau03);

        System.out.println();
        System.out.println("Fim do storyboard.");
    }

    // ---------------------------------------------------------------------
    // Métodos auxiliares de apresentação
    // ---------------------------------------------------------------------

    private static EscadaRolante cadastrar(Estacao estacao, int numero, GrupoManutencao grupo,
                                           Fabricante fabricante, String acesso) {
        EscadaRolante escada = new EscadaRolante(new CodigoEscada(estacao.getSigla(), numero),
                estacao, grupo, fabricante, acesso);
        estacao.adicionarEscada(escada);
        return escada;
    }

    private static List<Alerta> receber(EscadaRolante escada, DispositivoIoT dispositivo,
                                        Leitura leitura, PoliticaDeLimites politica) {
        dispositivo.registrarComunicacao(leitura.getDataHora());
        List<Alerta> alertas = escada.registrarLeitura(leitura, politica);
        System.out.println("  " + escada.getCodigo() + " → " + escada.getStatus()
                + (alertas.isEmpty() ? "" : "  | " + alertas.size() + " alerta(s): "
                + alertas.stream().map(a -> a.getNivel() + " " + a.descreverEvidencia())
                .collect(Collectors.joining("; "))));
        return alertas;
    }

    private static Alerta buscar(List<Alerta> alertas, EscadaRolante escada, String sinal, String nivel) {
        return alertas.stream()
                .filter(a -> a.getCodigoEscada().equals(escada.getCodigo())
                        && a.getSinal().name().equals(sinal) && a.getNivel().name().equals(nivel))
                .findFirst()
                .orElseThrow();
    }

    private static void tentar(String descricao, Runnable acao) {
        try {
            acao.run();
            System.out.println("  [!] " + descricao + ": nenhuma regra bloqueou.");
        } catch (DominioException e) {
            System.out.println("  [bloqueado] " + descricao);
            System.out.println("              " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void cena(int numero, String titulo) {
        System.out.println();
        System.out.println("CENA " + numero + " — " + titulo);
        System.out.println("-".repeat(60));
    }

    private static void imprimir(Object objeto) {
        System.out.println("  " + objeto);
    }
}
