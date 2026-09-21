package br.edu.pucminas.matricula.model;

import java.util.Objects;

/**
 * Especialização de Usuário representando um funcionário da Secretaria da universidade.
 * Responsável por cadastros básicos, geração de currículos semestrais e gestão do período de matrículas.
 */
public class Secretario extends Usuario {
    private String cargo;

    public Secretario(String id, String nome, String email, String senha, String cargo) {
        super(id, nome, email, senha);
        this.cargo = cargo;
    }

    /**
     * Stub para gerar um novo currículo para um semestre letivo.
     *
     * @param semestre Identificador do semestre (ex: "2026/2")
     * @return Instância do novo Curriculo
     */
    public Curriculo gerarCurriculoSemestre(String semestre) {
        return new Curriculo(semestre);
    }

    /**
     * Stub para abertura do período de matrículas de um currículo semestral.
     */
    public void abrirPeriodoMatricula(Curriculo curriculo) {
        if (curriculo != null) {
            curriculo.abrirPeriodo();
        }
    }

    /**
     * Stub para encerramento do período de matrículas de um currículo semestral.
     */
    public void fecharPeriodoMatricula(Curriculo curriculo) {
        if (curriculo != null) {
            curriculo.fecharPeriodo();
        }
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Secretario that = (Secretario) o;
        return Objects.equals(cargo, that.cargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cargo);
    }

    @Override
    public String toString() {
        return "Secretario{" +
                "id='" + getId() + '\'' +
                ", nome='" + getNome() + '\'' +
                ", cargo='" + cargo + '\'' +
                '}';
    }
}
