package br.com.fiap.motiva.escadas.teste;

import java.util.Objects;

/**
 * Pequeno apoio para as classes driver de teste: imprime [OK] ou [FALHOU] para cada
 * verificação e conta o resultado. Não usa bibliotecas externas.
 *
 * @author Squad (ver README)
 * @version 1.0
 * @since 1.0
 */
public final class Verificador {

    private int aprovados;
    private int reprovados;

    /**
     * @param titulo nome do conjunto de testes, impresso como cabeçalho
     */
    public Verificador(String titulo) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
    }

    /**
     * @param descricao o que está sendo verificado
     * @param condicao  resultado esperado é true
     */
    public void verificar(String descricao, boolean condicao) {
        if (condicao) {
            aprovados++;
            System.out.println("  [OK]     " + descricao);
        } else {
            reprovados++;
            System.out.println("  [FALHOU] " + descricao);
        }
    }

    /**
     * @param descricao o que está sendo verificado
     * @param esperado  valor esperado
     * @param obtido    valor obtido
     */
    public void verificarIgual(String descricao, Object esperado, Object obtido) {
        boolean igual = Objects.equals(esperado, obtido);
        verificar(igual ? descricao : descricao + " (esperado: " + esperado + ", obtido: " + obtido + ")", igual);
    }

    /**
     * Executa uma ação que deve lançar exceção e mostra a mensagem recebida.
     *
     * @param descricao regra que deve ser bloqueada
     * @param tipo      exceção esperada
     * @param acao      código que deve falhar
     */
    public void esperarExcecao(String descricao, Class<? extends RuntimeException> tipo, Runnable acao) {
        try {
            acao.run();
            verificar(descricao + " (nenhuma exceção foi lançada)", false);
        } catch (RuntimeException e) {
            if (tipo.isInstance(e)) {
                verificar(descricao, true);
                System.out.println("           -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                verificar(descricao + " (lançou " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")", false);
            }
        }
    }

    /**
     * Imprime o resumo do conjunto de testes.
     *
     * @return true se nenhuma verificação falhou
     */
    public boolean concluir() {
        System.out.println("  Resultado: " + aprovados + " aprovados, " + reprovados + " falharam");
        return reprovados == 0;
    }
}
