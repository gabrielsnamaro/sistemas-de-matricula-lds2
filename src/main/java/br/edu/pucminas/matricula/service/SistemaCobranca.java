package br.edu.pucminas.matricula.service;

import br.edu.pucminas.matricula.model.Aluno;
import br.edu.pucminas.matricula.model.Cobranca;
import br.edu.pucminas.matricula.model.Matricula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sistema funcional de Cobrança da universidade.
 * Notificado pelo sistema de matrículas após a inscrição do aluno para faturamento do semestre.
 */
public class SistemaCobranca {
    private final List<Cobranca> cobrancasEmitidas;

    public SistemaCobranca() {
        this.cobrancasEmitidas = new ArrayList<>();
    }

    /**
     * Gera e registra a cobrança para o aluno correspondente ao semestre e disciplinas matriculadas.
     *
     * @param aluno Aluno que realizou as matrículas
     * @param semestre Semestre de vigência das disciplinas
     * @param matriculas Lista de matrículas ativas
     * @return Cobranca emitida com o valor total calculado
     */
    public Cobranca gerarCobranca(Aluno aluno, String semestre, List<Matricula> matriculas) {
        if (aluno == null || semestre == null || matriculas == null || matriculas.isEmpty()) {
            return null;
        }
        Cobranca novaCobranca = new Cobranca(aluno, semestre, matriculas);
        this.cobrancasEmitidas.add(novaCobranca);
        notificarAluno(novaCobranca);
        return novaCobranca;
    }

    /**
     * Stub para notificação de cobrança enviada ao aluno (e-mail / notificação no sistema).
     *
     * @param cobranca Cobrança recém-emitida
     */
    public void notificarAluno(Cobranca cobranca) {
        if (cobranca != null && cobranca.getAluno() != null) {
            System.out.println("[SISTEMA DE COBRANÇA] Notificação enviada para " +
                    cobranca.getAluno().getNome() + " (" + cobranca.getAluno().getEmail() + "): " +
                    "Fatura gerada no valor de R$ " + String.format("%.2f", cobranca.getValorTotal()) +
                    " para o semestre " + cobranca.getSemestre());
        }
    }

    /**
     * Consulta as cobranças emitidas para um determinado aluno.
     *
     * @param aluno Aluno a consultar
     * @return Lista de cobranças emitidas
     */
    public List<Cobranca> consultarCobrancasPorAluno(Aluno aluno) {
        if (aluno == null) {
            return Collections.emptyList();
        }
        return this.cobrancasEmitidas.stream()
                .filter(c -> c.getAluno().equals(aluno))
                .collect(Collectors.toList());
    }

    public List<Cobranca> getCobrancasEmitidas() {
        return Collections.unmodifiableList(cobrancasEmitidas);
    }
}
