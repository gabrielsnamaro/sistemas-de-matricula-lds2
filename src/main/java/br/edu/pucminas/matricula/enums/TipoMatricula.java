package br.edu.pucminas.matricula.enums;

/**
 * Enum que representa a prioridade/tipo da matrícula feita pelo aluno.
 * Regra de negócio: até 4 obrigatórias (1ª opção) e até 2 optativas (alternativas).
 */
public enum TipoMatricula {
    OBRIGATORIA("Obrigatória (1ª Opção)"),
    OPTATIVA("Optativa (Alternativa)");

    private final String descricao;

    TipoMatricula(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
