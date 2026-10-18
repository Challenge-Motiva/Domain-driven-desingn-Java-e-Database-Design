package br.com.fiap.motiva.escadas.teste;

import br.com.fiap.motiva.escadas.dominio.entidade.Equipe;
import br.com.fiap.motiva.escadas.dominio.enums.Turno;
import br.com.fiap.motiva.escadas.dominio.excecao.EquipeIndisponivelException;
import br.com.fiap.motiva.escadas.dominio.excecao.ValorInvalidoException;

import java.time.LocalTime;

/**
 * Driver de teste isolado da entidade {@link Equipe}.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class TesteEquipe {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (!executar()) System.exit(1);
    }

    /** @return true se todos os testes passaram */
    public static boolean executar() {
        Verificador v = new Verificador("Equipe");

        Equipe equipe = new Equipe("Equipe Noturna Paulista", Turno.NOITE, Cenarios.paulista(), 3);
        System.out.println("           " + equipe);
        v.verificar("Equipe nova está disponível", equipe.isDisponivel());

        equipe.alocar();
        v.verificar("Equipe alocada fica indisponível", !equipe.isDisponivel());
        v.esperarExcecao("Equipe ocupada não pode ser alocada de novo", EquipeIndisponivelException.class,
                equipe::alocar);
        equipe.liberar();
        v.verificar("Equipe liberada volta a ficar disponível", equipe.isDisponivel());

        v.verificar("Turno da noite cobre as 02:00", equipe.atuaNoHorario(LocalTime.of(2, 0)));
        v.verificar("Turno da noite não cobre as 12:00", !equipe.atuaNoHorario(LocalTime.of(12, 0)));

        equipe.setQuantidadeTecnicos(4);
        v.verificarIgual("Quantidade de técnicos atualizada", 4, equipe.getQuantidadeTecnicos());
        v.esperarExcecao("Equipe sem técnicos é rejeitada", ValorInvalidoException.class,
                () -> equipe.setQuantidadeTecnicos(0));
        v.esperarExcecao("Equipe com 21 técnicos é rejeitada", ValorInvalidoException.class,
                () -> new Equipe("Equipe Gigante", Turno.MANHA, Cenarios.paulista(), 21));
        v.verificarIgual("Quantidade inválida não altera o valor anterior", 4, equipe.getQuantidadeTecnicos());

        return v.concluir();
    }
}
