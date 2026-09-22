package br.edu.pucminas.matricula.enums;

/**
 * Enum que indica a situação da disciplina no período de matrículas / semestre.
 * - PENDENTE_QUORUM: durante o período de matrículas, ainda aguardando quórum mínimo (3 alunos).
 * - ATIVA: quórum mínimo atingido e disciplina confirmada para o semestre.
 * - CANCELADA: não atingiu o quórum mínimo de 3 alunos ao fim do período.
 * - LOTADA: atingiu o limite máximo de 60 alunos matriculados, inscrições encerradas.
 */
public enum StatusDisciplina {
    PENDENTE_QUORUM("Pendente de Quórum Mínimo"),
    ATIVA("Ativa"),
    CANCELADA("Cancelada por falta de quórum"),
    LOTADA("Inscrições Encerradas (Turma Lotada)");

    private final String descricao;

    StatusDisciplina(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
