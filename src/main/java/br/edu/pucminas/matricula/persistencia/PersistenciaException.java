package br.edu.pucminas.matricula.persistencia;

/**
 * Exceção lançada quando ocorrem erros durante operações de persistência de dados.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

