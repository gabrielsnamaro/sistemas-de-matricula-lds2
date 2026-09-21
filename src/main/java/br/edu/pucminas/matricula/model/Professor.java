package br.edu.pucminas.matricula.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Especialização de Usuário representando um Professor da universidade.
 * Pode consultar quais são os alunos matriculados em cada uma de suas disciplinas.
 */
public class Professor extends Usuario {
    private String siape;
    private final List<Disciplina> disciplinasLecionadas;

    public Professor(String id, String nome, String email, String senha, String siape) {
        super(id, nome, email, senha);
        this.siape = siape;
        this.disciplinasLecionadas = new ArrayList<>();
    }

    /**
     * Stub para consultar a lista de alunos matriculados em uma disciplina ministrada pelo professor.
     *
     * @param disciplina Disciplina a ser consultada
     * @return Lista de alunos matriculados, ou lista vazia caso o professor não ministre a disciplina
     */
    public List<Aluno> consultarAlunos(Disciplina disciplina) {
        if (disciplina == null || !this.disciplinasLecionadas.contains(disciplina)) {
            return Collections.emptyList();
        }
        return disciplina.getAlunosMatriculados();
    }

    /**
     * Atribui uma nova disciplina para o professor ministrar.
     *
     * @param disciplina Disciplina a ser vinculada
     */
    public void atribuirDisciplina(Disciplina disciplina) {
        if (disciplina != null && !this.disciplinasLecionadas.contains(disciplina)) {
            this.disciplinasLecionadas.add(disciplina);
            disciplina.setProfessorResponsavel(this);
        }
    }

    /**
     * Remove uma disciplina da lista do professor.
     *
     * @param disciplina Disciplina a ser desvinculada
     */
    public void removerDisciplina(Disciplina disciplina) {
        if (disciplina != null && this.disciplinasLecionadas.remove(disciplina)) {
            if (disciplina.getProfessorResponsavel() == this) {
                disciplina.setProfessorResponsavel(null);
            }
        }
    }

    public String getSiape() {
        return siape;
    }

    public void setSiape(String siape) {
        this.siape = siape;
    }

    public List<Disciplina> getDisciplinasLecionadas() {
        return Collections.unmodifiableList(disciplinasLecionadas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Professor professor = (Professor) o;
        return Objects.equals(siape, professor.siape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), siape);
    }

    @Override
    public String toString() {
        return "Professor{" +
                "id='" + getId() + '\'' +
                ", siape='" + siape + '\'' +
                ", nome='" + getNome() + '\'' +
                ", totalDisciplinas=" + disciplinasLecionadas.size() +
                '}';
    }
}
