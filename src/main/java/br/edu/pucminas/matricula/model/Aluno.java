package br.edu.pucminas.matricula.model;

import br.edu.pucminas.matricula.enums.StatusMatricula;
import br.edu.pucminas.matricula.enums.TipoMatricula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Especialização de Usuário representando um Aluno da universidade.
 * Pode se matricular em até 4 disciplinas obrigatórias e até 2 optativas por período.
 */
public class Aluno extends Usuario {
    public static final int LIMITE_OBRIGATORIAS = 4;
    public static final int LIMITE_OPTATIVAS = 2;

    private String matricula;
    private Curso curso;
    private final List<Matricula> matriculas;

    public Aluno(String id, String nome, String email, String senha, String matricula, Curso curso) {
        super(id, nome, email, senha);
        this.matricula = matricula;
        this.curso = curso;
        this.matriculas = new ArrayList<>();
    }

    /**
     * Stub para efetuar matrícula em uma disciplina com validação das regras de limite:
     * Máximo 4 obrigatórias e 2 optativas.
     *
     * @param disciplina Disciplina desejada
     * @param tipo Tipo da opção (OBRIGATORIA ou OPTATIVA)
     * @return a Matricula gerada, ou null se infringir os limites ou não houver vagas
     */
    public Matricula matricular(Disciplina disciplina, TipoMatricula tipo) {
        if (disciplina == null || tipo == null) {
            return null;
        }

        // Validação de limite por tipo
        int qtdAtual = getQuantidadeMatriculasPorTipo(tipo);
        int limite = (tipo == TipoMatricula.OBRIGATORIA) ? LIMITE_OBRIGATORIAS : LIMITE_OPTATIVAS;
        if (qtdAtual >= limite) {
            return null;
        }

        // Verifica se já está matriculado ativamente nesta disciplina
        boolean jaMatriculado = this.matriculas.stream()
                .anyMatch(m -> m.getDisciplina().equals(disciplina) && m.getStatus() == StatusMatricula.ATIVA);
        if (jaMatriculado) {
            return null;
        }

        Matricula novaMatricula = new Matricula(this, disciplina, tipo);
        boolean sucessoNaDisciplina = disciplina.adicionarMatricula(novaMatricula);

        if (sucessoNaDisciplina) {
            this.matriculas.add(novaMatricula);
            return novaMatricula;
        }

        return null;
    }

    /**
     * Stub para cancelar uma matrícula ativa em disciplina.
     *
     * @param disciplina Disciplina a ter matrícula cancelada
     * @return true se cancelada com sucesso, false caso não estivesse matriculado
     */
    public boolean cancelarMatricula(Disciplina disciplina) {
        if (disciplina == null) {
            return false;
        }
        for (Matricula m : this.matriculas) {
            if (m.getDisciplina().equals(disciplina) && m.getStatus() == StatusMatricula.ATIVA) {
                return m.cancelar();
            }
        }
        return false;
    }

    /**
     * Retorna a quantidade de matrículas ativas de determinado tipo (obrigatória ou optativa).
     */
    public int getQuantidadeMatriculasPorTipo(TipoMatricula tipo) {
        return (int) this.matriculas.stream()
                .filter(m -> m.getStatus() == StatusMatricula.ATIVA && m.getTipo() == tipo)
                .count();
    }

    /**
     * Retorna lista de disciplinas em que o aluno está atualmente matriculado e ativo.
     */
    public List<Disciplina> getDisciplinasMatriculadas() {
        List<Disciplina> disciplinas = new ArrayList<>();
        for (Matricula m : this.matriculas) {
            if (m.getStatus() == StatusMatricula.ATIVA) {
                disciplinas.add(m.getDisciplina());
            }
        }
        return Collections.unmodifiableList(disciplinas);
    }

    /**
     * Calcula o total de créditos das disciplinas em que o aluno está matriculado ativamente.
     */
    public int calcularTotalCreditos() {
        return getDisciplinasMatriculadas().stream()
                .mapToInt(Disciplina::getCreditos)
                .sum();
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<Matricula> getMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Aluno aluno = (Aluno) o;
        return Objects.equals(matricula, aluno.matricula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), matricula);
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "id='" + getId() + '\'' +
                ", matricula='" + matricula + '\'' +
                ", nome='" + getNome() + '\'' +
                ", curso=" + (curso != null ? curso.getNome() : "null") +
                ", ativas=" + getDisciplinasMatriculadas().size() +
                '}';
    }
}
