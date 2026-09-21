package br.edu.pucminas.matricula.model;

import br.edu.pucminas.matricula.enums.StatusMatricula;
import br.edu.pucminas.matricula.enums.TipoMatricula;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe associativa que representa a matrícula de um Aluno em uma Disciplina.
 * Contém o tipo da opção (obrigatória/optativa), a data e o status da matrícula.
 */
public class Matricula {
    private String id;
    private Aluno aluno;
    private Disciplina disciplina;
    private TipoMatricula tipo;
    private StatusMatricula status;
    private LocalDateTime dataMatricula;

    public Matricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        this.id = UUID.randomUUID().toString();
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.tipo = tipo;
        this.status = StatusMatricula.ATIVA;
        this.dataMatricula = LocalDateTime.now();
    }

    public Matricula(String id, Aluno aluno, Disciplina disciplina, TipoMatricula tipo, StatusMatricula status, LocalDateTime dataMatricula) {
        this.id = id;
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.tipo = tipo;
        this.status = status;
        this.dataMatricula = dataMatricula;
    }

    /**
     * Stub para cancelamento da matrícula pelo aluno durante o período permitido.
     *
     * @return true se o cancelamento foi efetuado com sucesso, false caso já estivesse cancelada
     */
    public boolean cancelar() {
        if (this.status == StatusMatricula.CANCELADA) {
            return false;
        }
        this.status = StatusMatricula.CANCELADA;
        if (this.disciplina != null) {
            this.disciplina.removerMatricula(this);
        }
        return true;
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

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public TipoMatricula getTipo() {
        return tipo;
    }

    public void setTipo(TipoMatricula tipo) {
        this.tipo = tipo;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    public LocalDateTime getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(LocalDateTime dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matricula matricula = (Matricula) o;
        return Objects.equals(id, matricula.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Matricula{" +
                "id='" + id + '\'' +
                ", aluno=" + (aluno != null ? aluno.getNome() : "null") +
                ", disciplina=" + (disciplina != null ? disciplina.getNome() : "null") +
                ", tipo=" + tipo +
                ", status=" + status +
                ", dataMatricula=" + dataMatricula +
                '}';
    }
}
