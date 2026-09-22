package br.edu.pucminas.matricula.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa o Currículo gerado pela Secretaria para um determinado semestre.
 * Mantém as disciplinas ofertadas e o controle do período de matrículas.
 */
public class Curriculo {
    private String semestre;
    private final List<Disciplina> disciplinasOfertadas;
    private boolean periodoMatriculaAberto;

    public Curriculo(String semestre) {
        this.semestre = semestre;
        this.disciplinasOfertadas = new ArrayList<>();
        this.periodoMatriculaAberto = false;
    }

    /**
     * Adiciona uma disciplina à oferta do semestre.
     *
     * @param disciplina Disciplina ofertada
     */
    public void adicionarOferta(Disciplina disciplina) {
        if (disciplina != null && !this.disciplinasOfertadas.contains(disciplina)) {
            this.disciplinasOfertadas.add(disciplina);
        }
    }

    /**
     * Remove uma disciplina da oferta do semestre.
     *
     * @param disciplina Disciplina a ser removida
     */
    public void removerOferta(Disciplina disciplina) {
        this.disciplinasOfertadas.remove(disciplina);
    }

    /**
     * Abre o período de matrículas para os alunos realizarem inscrições e cancelamentos.
     */
    public void abrirPeriodo() {
        this.periodoMatriculaAberto = true;
    }

    /**
     * Encerra o período de matrículas e executa a checagem automática de quórum.
     */
    public void fecharPeriodo() {
        this.periodoMatriculaAberto = false;
        processarQuorumDisciplinas();
    }

    /**
     * Stub para processar a verificação de quórum de todas as disciplinas ofertadas.
     * Disciplinas com menos de 3 alunos inscritos serão canceladas.
     */
    public void processarQuorumDisciplinas() {
        for (Disciplina d : this.disciplinasOfertadas) {
            d.verificarQuorum();
        }
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public List<Disciplina> getDisciplinasOfertadas() {
        return Collections.unmodifiableList(disciplinasOfertadas);
    }

    public boolean isPeriodoMatriculaAberto() {
        return periodoMatriculaAberto;
    }

    public void setPeriodoMatriculaAberto(boolean periodoMatriculaAberto) {
        this.periodoMatriculaAberto = periodoMatriculaAberto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curriculo curriculo = (Curriculo) o;
        return Objects.equals(semestre, curriculo.semestre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(semestre);
    }

    @Override
    public String toString() {
        return "Curriculo{" +
                "semestre='" + semestre + '\'' +
                ", totalOfertas=" + disciplinasOfertadas.size() +
                ", periodoAberto=" + periodoMatriculaAberto +
                '}';
    }
}
