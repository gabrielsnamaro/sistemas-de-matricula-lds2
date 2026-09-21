package br.edu.pucminas.matricula.model;

import br.edu.pucminas.matricula.enums.StatusMatricula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa a Cobrança/Fatura gerada pelo Sistema de Cobrança para um aluno
 * com base nas disciplinas matriculadas no semestre.
 */
public class Cobranca {
    public static final double VALOR_POR_CREDITO = 150.00;

    private String id;
    private Aluno aluno;
    private String semestre;
    private double valorTotal;
    private LocalDateTime dataEmissao;
    private boolean paga;
    private final List<Matricula> matriculasCobradas;

    public Cobranca(Aluno aluno, String semestre, List<Matricula> matriculas) {
        this.id = UUID.randomUUID().toString();
        this.aluno = aluno;
        this.semestre = semestre;
        this.dataEmissao = LocalDateTime.now();
        this.paga = false;
        this.matriculasCobradas = new ArrayList<>();
        if (matriculas != null) {
            for (Matricula m : matriculas) {
                if (m.getStatus() == StatusMatricula.ATIVA) {
                    this.matriculasCobradas.add(m);
                }
            }
        }
        this.valorTotal = calcularValor(this.matriculasCobradas);
    }

    /**
     * Calcula o valor financeiro total da cobrança com base nos créditos das disciplinas matriculadas.
     *
     * @param matriculas Lista de matrículas ativas a serem cobradas
     * @return Valor total calculado em reais
     */
    public double calcularValor(List<Matricula> matriculas) {
        if (matriculas == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Matricula m : matriculas) {
            if (m.getDisciplina() != null) {
                total += m.getDisciplina().getCreditos() * VALOR_POR_CREDITO;
            }
        }
        return total;
    }

    /**
     * Stub para registrar a quitação/pagamento desta cobrança.
     */
    public void registrarPagamento() {
        this.paga = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public boolean isPaga() {
        return paga;
    }

    public void setPaga(boolean paga) {
        this.paga = paga;
    }

    public List<Matricula> getMatriculasCobradas() {
        return Collections.unmodifiableList(matriculasCobradas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cobranca cobranca = (Cobranca) o;
        return Objects.equals(id, cobranca.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cobranca{" +
                "id='" + id + '\'' +
                ", aluno=" + (aluno != null ? aluno.getNome() : "null") +
                ", semestre='" + semestre + '\'' +
                ", valorTotal=R$ " + String.format("%.2f", valorTotal) +
                ", paga=" + paga +
                '}';
    }
}
