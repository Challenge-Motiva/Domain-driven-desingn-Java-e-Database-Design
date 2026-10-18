package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.enums.Fabricante;
import br.com.fiap.motiva.escadas.dominio.enums.GrupoManutencao;
import br.com.fiap.motiva.escadas.dominio.enums.NivelAlerta;
import br.com.fiap.motiva.escadas.dominio.enums.StatusCondicao;
import br.com.fiap.motiva.escadas.dominio.excecao.LeituraInvalidaException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.regra.PoliticaDeLimites;
import br.com.fiap.motiva.escadas.dominio.regra.RegraDeLimite;
import br.com.fiap.motiva.escadas.dominio.vo.CodigoEscada;
import br.com.fiap.motiva.escadas.dominio.vo.Medicao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Entidade central do domínio: uma escada rolante monitorada.
 * <p>
 * A escada recebe leituras, avalia cada sinal contra a política de limites,
 * gera alertas e atualiza a própria condição (o semáforo do dashboard).
 * O status não tem setter: ele só muda por meio dos comportamentos
 * {@link #registrarLeitura} e {@link #verificarInatividade}.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class EscadaRolante {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private final CodigoEscada codigo;
    private final Estacao estacao;
    private final GrupoManutencao grupo;
    private final Fabricante fabricante;
    private final String acesso;
    private StatusCondicao status;
    private LocalDateTime ultimaLeitura;

    /**
     * Cadastro de uma escada nova. Começa como SEM_DADOS (cinza) até a primeira leitura.
     *
     * @param codigo     código no padrão ECR-XXX-00
     * @param estacao    estação onde a escada está instalada
     * @param grupo      grupo de manutenção
     * @param fabricante fabricante do equipamento
     * @param acesso     ligação que a escada faz (ex.: "Plataforma → mezanino")
     * @throws ValorInvalidoException se faltar dado ou o código não for da estação informada
     */
    public EscadaRolante(CodigoEscada codigo, Estacao estacao, GrupoManutencao grupo,
                         Fabricante fabricante, String acesso) {
        this(null, codigo, estacao, grupo, fabricante, acesso, StatusCondicao.SEM_DADOS, null);
    }

    /**
     * Reconstrução de uma escada já existente, com status e última leitura.
     *
     * @param id            identificador no banco
     * @param codigo        código no padrão ECR-XXX-00
     * @param estacao       estação onde a escada está instalada
     * @param grupo         grupo de manutenção
     * @param fabricante    fabricante do equipamento
     * @param acesso        ligação que a escada faz
     * @param status        condição atual
     * @param ultimaLeitura instante da última leitura recebida, ou null se nunca recebeu
     * @throws ValorInvalidoException se faltar dado ou houver incoerência
     */
    public EscadaRolante(Long id, CodigoEscada codigo, Estacao estacao, GrupoManutencao grupo,
                         Fabricante fabricante, String acesso, StatusCondicao status, LocalDateTime ultimaLeitura) {
        if (id != null && id <= 0) {
            throw new ValorInvalidoException("O id da escada deve ser positivo.");
        }
        if (codigo == null || estacao == null || grupo == null || fabricante == null || status == null) {
            throw new ValorInvalidoException("Código, estação, grupo, fabricante e status da escada são obrigatórios.");
        }
        if (!codigo.getSiglaEstacao().equals(estacao.getSigla())) {
            throw new ValorInvalidoException("O código " + codigo + " indica a estação "
                    + codigo.getSiglaEstacao() + ", mas a escada foi associada a " + estacao.getNome()
                    + " (" + estacao.getSigla() + ").");
        }
        if (acesso == null || acesso.isBlank()) {
            throw new ValorInvalidoException("Descreva o acesso que a escada atende (ex.: Plataforma → mezanino).");
        }
        if (acesso.trim().length() > 100) {
            throw new ValorInvalidoException("A descrição do acesso deve ter no máximo 100 caracteres.");
        }
        if (ultimaLeitura == null && status != StatusCondicao.SEM_DADOS) {
            throw new ValorInvalidoException("Uma escada que nunca enviou leitura só pode estar SEM_DADOS.");
        }
        this.id = id;
        this.codigo = codigo;
        this.estacao = estacao;
        this.grupo = grupo;
        this.fabricante = fabricante;
        this.acesso = acesso.trim();
        this.status = status;
        this.ultimaLeitura = ultimaLeitura;
    }

    /**
     * Recebe uma leitura, avalia os seis sinais e atualiza a condição da escada.
     * A condição passa a ser a do pior sinal da leitura.
     *
     * @param leitura  leitura recebida
     * @param politica limites usados na avaliação
     * @return alertas gerados (lista vazia se todos os sinais estão normais)
     * @throws LeituraInvalidaException se a leitura for de outra escada ou estiver fora de ordem
     * @throws ValorInvalidoException   se a política for nula
     */
    public List<Alerta> registrarLeitura(Leitura leitura, PoliticaDeLimites politica) {
        if (leitura == null) {
            throw new LeituraInvalidaException("Nenhuma leitura recebida para " + codigo + ".");
        }
        if (politica == null) {
            throw new ValorInvalidoException("É preciso uma política de limites para avaliar a leitura.");
        }
        if (!leitura.getCodigoEscada().equals(codigo)) {
            throw new LeituraInvalidaException("A leitura de " + leitura.getCodigoEscada()
                    + " foi enviada para a escada " + codigo + ".");
        }
        if (ultimaLeitura != null && !leitura.getDataHora().isAfter(ultimaLeitura)) {
            throw new LeituraInvalidaException("Leitura fora de ordem para " + codigo + ": "
                    + leitura.getDataHora().format(FORMATO) + " não é posterior à última ("
                    + ultimaLeitura.format(FORMATO) + ").");
        }

        List<Alerta> alertas = new ArrayList<>();
        StatusCondicao piorStatus = StatusCondicao.NORMAL;
        for (Medicao medicao : leitura.getMedicoes()) {
            RegraDeLimite regra = politica.regraPara(medicao.getTipo());
            Optional<NivelAlerta> nivel = regra.avaliar(medicao);
            if (nivel.isPresent()) {
                double limite = regra.limiteViolado(medicao.getValor(), nivel.get());
                alertas.add(new Alerta(leitura, medicao, nivel.get(), limite));
                StatusCondicao statusDoSinal = nivel.get().getStatusCorrespondente();
                if (statusDoSinal.isMaisGraveQue(piorStatus)) {
                    piorStatus = statusDoSinal;
                }
            }
        }
        this.status = piorStatus;
        this.ultimaLeitura = leitura.getDataHora();
        return alertas;
    }

    /**
     * Marca a escada como SEM_DADOS (cinza) se ela parou de enviar leituras.
     * Sensor offline é uma situação real e não pode aparecer como "verde".
     *
     * @param agora      instante de referência
     * @param tolerancia tempo máximo aceitável sem leitura
     * @return true se a escada está sem dados após a verificação
     * @throws ValorInvalidoException se os parâmetros forem nulos ou a tolerância não for positiva
     */
    public boolean verificarInatividade(LocalDateTime agora, Duration tolerancia) {
        if (agora == null || tolerancia == null || tolerancia.isNegative() || tolerancia.isZero()) {
            throw new ValorInvalidoException("Informe o instante atual e uma tolerância positiva.");
        }
        if (ultimaLeitura == null || Duration.between(ultimaLeitura, agora).compareTo(tolerancia) > 0) {
            this.status = StatusCondicao.SEM_DADOS;
            return true;
        }
        return false;
    }

    /**
     * Prioridade usada no ranking do dashboard. Primeiro pesa a gravidade do status;
     * no mesmo status, a escada crítica vem antes da não crítica (RN01).
     *
     * @return número maior significa atender antes
     */
    public int calcularPrioridade() {
        return status.getPeso() * 2 + (isCritica() ? 1 : 0);
    }

    /**
     * @param agora instante de referência
     * @return tempo desde a última leitura, ou vazio se a escada nunca enviou dados
     */
    public Optional<Duration> calcularTempoDesdeUltimaLeitura(LocalDateTime agora) {
        if (ultimaLeitura == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(ultimaLeitura, agora));
    }

    /** @return true se a escada pertence a um grupo crítico */
    public boolean isCritica() {
        return grupo.isCritica();
    }

    /** @return id no banco, ou null se a escada ainda não foi gravada */
    public Long getId() {
        return id;
    }

    /** @return código da escada */
    public CodigoEscada getCodigo() {
        return codigo;
    }

    /** @return estação onde a escada está instalada */
    public Estacao getEstacao() {
        return estacao;
    }

    /** @return grupo de manutenção */
    public GrupoManutencao getGrupo() {
        return grupo;
    }

    /** @return fabricante do equipamento */
    public Fabricante getFabricante() {
        return fabricante;
    }

    /** @return descrição do acesso atendido pela escada */
    public String getAcesso() {
        return acesso;
    }

    /** @return condição atual */
    public StatusCondicao getStatus() {
        return status;
    }

    /** @return instante da última leitura, ou null */
    public LocalDateTime getUltimaLeitura() {
        return ultimaLeitura;
    }

    @Override
    public String toString() {
        return "EscadaRolante{codigo=" + codigo
                + ", estacao='" + estacao.getNome() + "'"
                + ", grupo=" + grupo
                + ", fabricante=" + fabricante
                + ", acesso='" + acesso + "'"
                + ", status=" + status
                + ", ultimaLeitura=" + (ultimaLeitura == null ? "nunca" : ultimaLeitura.format(FORMATO))
                + "}";
    }
}
