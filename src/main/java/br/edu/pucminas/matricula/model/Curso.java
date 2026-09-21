package br.edu.pucminas.matricula.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa um Curso da Universidade.
 * Cada curso tem um nome, um determinado número de créditos e é constituído por diversas disciplinas.
 */
public class Curso {
    private String codigo;
    private String nome;
    private int totalCreditos;
    private final List<Disciplina> gradeDisciplinas;

    public Curso(String codigo, String nome, int totalCreditos) {
        this.codigo = codigo;
        this.nome = nome;
        this.totalCreditos = totalCreditos;
        this.gradeDisciplinas = new ArrayList<>();
    }

    /**
     * Stub para adicionar uma disciplina à grade curricular do curso.
     *
     * @param disciplina Disciplina a ser associada ao curso
     */
    public void adicionarDisciplina(Disciplina disciplina) {
        if (disciplina != null && !this.gradeDisciplinas.contains(disciplina)) {
            this.gradeDisciplinas.add(disciplina);
        }
    }

    /**
     * Stub para remover uma disciplina da grade do curso.
     *
     * @param disciplina Disciplina a ser removida
     */
    public void removerDisciplina(Disciplina disciplina) {
        this.gradeDisciplinas.remove(disciplina);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(int totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public List<Disciplina> getGradeDisciplinas() {
        return Collections.unmodifiableList(gradeDisciplinas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curso curso = (Curso) o;
        return Objects.equals(codigo, curso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Curso{" +
                "codigo='" + codigo + '\'' +
                ", nome='" + nome + '\'' +
                ", totalCreditos=" + totalCreditos +
                ", disciplinasCount=" + gradeDisciplinas.size() +
                '}';
    }
}
