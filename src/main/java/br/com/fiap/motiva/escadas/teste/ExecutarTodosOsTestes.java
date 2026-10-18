package br.com.fiap.motiva.escadas.teste;

/**
 * Executa todas as classes driver de teste em sequência e mostra o resultado geral.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public class ExecutarTodosOsTestes {

    /**
     * @param args não utilizado
     */
    public static void main(String[] args) {
        boolean[] resultados = {
                TesteValueObjects.executar(),
                TesteRegrasDeLimite.executar(),
                TesteEstacao.executar(),
                TesteLeitura.executar(),
                TesteEscadaRolante.executar(),
                TesteAlerta.executar(),
                TesteEquipe.executar(),
                TesteOrdemServico.executar(),
                TesteDispositivoIoT.executar()
        };
        int falhas = 0;
        for (boolean ok : resultados) {
            if (!ok) falhas++;
        }
        System.out.println();
        System.out.println("=====================================================");
        System.out.println(falhas == 0
                ? "Todos os " + resultados.length + " conjuntos de teste passaram."
                : falhas + " de " + resultados.length + " conjuntos de teste tiveram falhas.");
        System.out.println("=====================================================");
        if (falhas > 0) System.exit(1);
    }
}
