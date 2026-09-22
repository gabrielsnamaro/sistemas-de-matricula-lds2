package br.edu.pucminas.matricula.enums;

/**
 * Enum que indica o estado atual de uma matrícula.
 */
public enum StatusMatricula {
    ATIVA("Ativa"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusMatricula(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
