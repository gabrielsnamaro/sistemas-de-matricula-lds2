package br.edu.pucminas.matricula.model;

import br.edu.pucminas.matricula.enums.StatusDisciplina;
import br.edu.pucminas.matricula.enums.StatusMatricula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa uma Disciplina ofertada pela universidade.
 * Regras de negócio atendidas:
 * - Mínimo de 3 alunos para confirmação (quórum) no fim do período de matrículas.
 * - Máximo de 60 alunos para encerramento de inscrições (turma lotada).
 */
public class Disciplina {
    public static final int MIN_ALUNOS = 3;
    public static final int MAX_ALUNOS = 60;

    private String codigo;
    private String nome;
    private int creditos;
    private Professor professorResponsavel;
    private final List<Matricula> inscricoes;
    private StatusDisciplina status;

    public Disciplina(String codigo, String nome, int creditos) {
        this.codigo = codigo;
        this.nome = nome;
        this.creditos = creditos;
        this.inscricoes = new ArrayList<>();
        this.status = StatusDisciplina.PENDENTE_QUORUM;
    }

    public Disciplina(String codigo, String nome, int creditos, Professor professorResponsavel) {
        this(codigo, nome, creditos);
        this.professorResponsavel = professorResponsavel;
    }

    /**
     * Stub para adicionar uma nova matrícula à disciplina, validando capacidade máxima.
     *
     * @param matricula Matrícula a ser vinculada
     * @return true se foi adicionada, false se a turma já estiver lotada ou se a disciplina estiver cancelada
     */
    public boolean adicionarMatricula(Matricula matricula) {
        if (matricula == null || this.status == StatusDisciplina.CANCELADA) {
            return false;
        }
        if (!temVagas()) {
            this.status = StatusDisciplina.LOTADA;
            return false;
        }
        if (!this.inscricoes.contains(matricula)) {
            this.inscricoes.add(matricula);
            if (this.inscricoes.size() >= MAX_ALUNOS) {
                this.status = StatusDisciplina.LOTADA;
            }
            return true;
        }
        return false;
    }

    /**
     * Stub para remover uma matrícula da disciplina (por cancelamento do aluno).
     *
     * @param matricula Matrícula a ser removida
     * @return true se removida com sucesso
     */
    public boolean removerMatricula(Matricula matricula) {
        boolean removido = this.inscricoes.remove(matricula);
        if (removido && this.status == StatusDisciplina.LOTADA && temVagas()) {
            this.status = this.inscricoes.size() >= MIN_ALUNOS ? StatusDisciplina.ATIVA : StatusDisciplina.PENDENTE_QUORUM;
        }
        return removido;
    }

    /**
     * Stub para verificação do quórum mínimo (3 alunos) ao término do período de matrículas.
     * Caso tenha menos de 3 alunos inscritos, a disciplina é cancelada.
     *
     * @return true se o quórum foi atingido e a disciplina foi ativada, false se foi cancelada
     */
    public boolean verificarQuorum() {
        int totalAtivos = (int) this.inscricoes.stream()
                .filter(m -> m.getStatus() == StatusMatricula.ATIVA)
                .count();

        if (totalAtivos >= MIN_ALUNOS) {
            if (this.status != StatusDisciplina.LOTADA) {
                this.status = StatusDisciplina.ATIVA;
            }
            return true;
        } else {
            this.status = StatusDisciplina.CANCELADA;
            return false;
        }
    }

    /**
     * Verifica se a disciplina ainda possui vagas disponíveis (limite de 60 alunos).
     *
     * @return true se houver vagas disponíveis
     */
    public boolean temVagas() {
        long totalAtivos = this.inscricoes.stream()
                .filter(m -> m.getStatus() == StatusMatricula.ATIVA)
                .count();
        return totalAtivos < MAX_ALUNOS;
    }

    /**
     * Retorna a quantidade de alunos matriculados ativos.
     */
    public int getQuantidadeInscritos() {
        return (int) this.inscricoes.stream()
                .filter(m -> m.getStatus() == StatusMatricula.ATIVA)
                .count();
    }

    /**
     * Retorna a lista de alunos matriculados nesta disciplina.
     */
    public List<Aluno> getAlunosMatriculados() {
        List<Aluno> alunos = new ArrayList<>();
        for (Matricula m : this.inscricoes) {
            if (m.getStatus() == StatusMatricula.ATIVA && m.getAluno() != null) {
                alunos.add(m.getAluno());
            }
        }
        return Collections.unmodifiableList(alunos);
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

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public Professor getProfessorResponsavel() {
        return professorResponsavel;
    }

    public void setProfessorResponsavel(Professor professorResponsavel) {
        this.professorResponsavel = professorResponsavel;
    }

    public List<Matricula> getInscricoes() {
        return Collections.unmodifiableList(inscricoes);
    }

    public StatusDisciplina getStatus() {
        return status;
    }

    public void setStatus(StatusDisciplina status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disciplina that = (Disciplina) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Disciplina{" +
                "codigo='" + codigo + '\'' +
                ", nome='" + nome + '\'' +
                ", creditos=" + creditos +
                ", status=" + status +
                ", inscritos=" + getQuantidadeInscritos() +
                '}';
    }
}
