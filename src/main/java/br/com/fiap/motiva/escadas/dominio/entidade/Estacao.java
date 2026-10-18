package br.com.fiap.motiva.escadas.dominio.entidade;

import br.com.fiap.motiva.escadas.dominio.enums.Linha;
import br.com.fiap.motiva.escadas.dominio.enums.StatusCondicao;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;
import br.com.fiap.motiva.escadas.dominio.vo.SiglaEstacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entidade: estação onde as escadas rolantes estão instaladas. É a classe de
 * localização do domínio e o ponto de agrupamento do dashboard (um card por estação).
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class Estacao {

    private Long id;
    private final String nome;
    private final SiglaEstacao sigla;
    private final Linha linha;
    private final List<EscadaRolante> escadas = new ArrayList<>();

    /**
     * Cadastro de uma estação nova, ainda sem identificador no banco.
     *
     * @param nome  nome da estação (ex.: "Paulista")
     * @param sigla sigla de 3 letras
     * @param linha linha à qual a estação pertence
     * @throws ValorInvalidoException se algum dado obrigatório faltar
     */
    public Estacao(String nome, SiglaEstacao sigla, Linha linha) {
        this(null, nome, sigla, linha);
    }

    /**
     * Reconstrução de uma estação já existente (vinda do banco ou do dataset).
     *
     * @param id    identificador no banco
     * @param nome  nome da estação
     * @param sigla sigla de 3 letras
     * @param linha linha à qual a estação pertence
     * @throws ValorInvalidoException se algum dado obrigatório faltar ou o id for inválido
     */
    public Estacao(Long id, String nome, SiglaEstacao sigla, Linha linha) {
        if (id != null && id <= 0) {
            throw new ValorInvalidoException("O id da estação deve ser positivo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new ValorInvalidoException("O nome da estação é obrigatório.");
        }
        if (nome.trim().length() > 80) {
            throw new ValorInvalidoException("O nome da estação deve ter no máximo 80 caracteres.");
        }
        if (sigla == null) {
            throw new ValorInvalidoException("A sigla da estação é obrigatória.");
        }
        if (linha == null) {
            throw new ValorInvalidoException("A linha da estação é obrigatória.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.sigla = sigla;
        this.linha = linha;
    }

    /**
     * Vincula uma escada a esta estação.
     *
     * @param escada escada criada com esta estação
     * @throws ValorInvalidoException se a escada pertencer a outra estação ou já estiver cadastrada
     */
    public void adicionarEscada(EscadaRolante escada) {
        if (escada == null) {
            throw new ValorInvalidoException("Não é possível adicionar uma escada nula.");
        }
        if (escada.getEstacao() != this) {
            throw new ValorInvalidoException("A escada " + escada.getCodigo()
                    + " pertence à estação " + escada.getEstacao().getNome() + ", não a " + nome + ".");
        }
        boolean repetida = escadas.stream().anyMatch(e -> e.getCodigo().equals(escada.getCodigo()));
        if (repetida) {
            throw new ValorInvalidoException("A escada " + escada.getCodigo() + " já está cadastrada em " + nome + ".");
        }
        escadas.add(escada);
    }

    /**
     * Resumo usado no card da estação no dashboard.
     *
     * @return quantidade de escadas em cada status (todos os status aparecem, mesmo com zero)
     */
    public Map<StatusCondicao, Long> contarEscadasPorStatus() {
        Map<StatusCondicao, Long> contagem = new EnumMap<>(StatusCondicao.class);
        for (StatusCondicao status : StatusCondicao.values()) {
            contagem.put(status, 0L);
        }
        for (EscadaRolante escada : escadas) {
            contagem.merge(escada.getStatus(), 1L, Long::sum);
        }
        return contagem;
    }

    /**
     * @return escadas em amarelo ou vermelho, da mais prioritária para a menos
     */
    public List<EscadaRolante> listarEscadasQueExigemAtencao() {
        return escadas.stream()
                .filter(e -> e.getStatus() == StatusCondicao.ATENCAO
                        || e.getStatus() == StatusCondicao.INTERVENCAO_RECOMENDADA)
                .sorted(Comparator.comparingInt(EscadaRolante::calcularPrioridade).reversed())
                .collect(Collectors.toList());
    }

    /** @return id no banco, ou null se a estação ainda não foi gravada */
    public Long getId() {
        return id;
    }

    /** @return nome da estação */
    public String getNome() {
        return nome;
    }

    /** @return sigla de 3 letras */
    public SiglaEstacao getSigla() {
        return sigla;
    }

    /** @return linha à qual a estação pertence */
    public Linha getLinha() {
        return linha;
    }

    /** @return escadas da estação (lista somente leitura) */
    public List<EscadaRolante> getEscadas() {
        return Collections.unmodifiableList(escadas);
    }

    @Override
    public String toString() {
        return "Estacao{id=" + id
                + ", nome='" + nome + "'"
                + ", sigla=" + sigla
                + ", linha=" + linha
                + ", escadas=" + escadas.size()
                + "}";
    }
}
