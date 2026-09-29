package br.edu.pucminas.matricula.persistencia;

import br.edu.pucminas.matricula.service.SistemaMatricula;

import java.io.IOException;

/**
 * Interface que define o contrato do módulo de persistência de dados do sistema.
 * Permite salvar e carregar o estado completo das entidades universitárias em arquivo.
 */
public interface PersistenciaDados {

    /**
     * Salva o estado completo do sistema no arquivo padrão.
     *
     * @param sistema Instância do sistema a ser persistida
     * @throws IOException Caso ocorra falha de E/S na gravação
     */
    void salvar(SistemaMatricula sistema) throws IOException;

    /**
     * Salva o estado completo do sistema em um arquivo especificado.
     *
     * @param sistema Instância do sistema a ser persistida
     * @param caminhoArquivo Caminho do arquivo de destino
     * @throws IOException Caso ocorra falha de E/S na gravação
     */
    void salvar(SistemaMatricula sistema, String caminhoArquivo) throws IOException;

    /**
     * Carrega os dados a partir do arquivo padrão e popula a instância fornecida.
     *
     * @param sistema Instância do sistema a ser populada
     * @throws IOException Caso ocorra falha de E/S na leitura
     */
    void carregar(SistemaMatricula sistema) throws IOException;

    /**
     * Carrega os dados a partir do arquivo especificado e popula a instância fornecida.
     *
     * @param sistema Instância do sistema a ser populada
     * @param caminhoArquivo Caminho do arquivo de origem
     * @throws IOException Caso ocorra falha de E/S na leitura
     */
    void carregar(SistemaMatricula sistema, String caminhoArquivo) throws IOException;

    /**
     * Cria e retorna uma nova instância do sistema populada com os dados do arquivo padrão.
     *
     * @return Nova instância de SistemaMatricula
     * @throws IOException Caso ocorra falha de E/S na leitura
     */
    SistemaMatricula carregar() throws IOException;

    /**
     * Cria e retorna uma nova instância do sistema populada com os dados do arquivo especificado.
     *
     * @param caminhoArquivo Caminho do arquivo de origem
     * @return Nova instância de SistemaMatricula
     * @throws IOException Caso ocorra falha de E/S na leitura
     */
    SistemaMatricula carregar(String caminhoArquivo) throws IOException;

    /**
     * Verifica se o arquivo de persistência padrão existe no sistema de arquivos.
     *
     * @return true se o arquivo existir, false caso contrário
     */
    boolean existeArquivoPersistencia();

    /**
     * Verifica se o arquivo especificado existe no sistema de arquivos.
     *
     * @param caminhoArquivo Caminho do arquivo
     * @return true se o arquivo existir, false caso contrário
     */
    boolean existeArquivoPersistencia(String caminhoArquivo);

    /**
     * Retorna o caminho do arquivo de persistência padrão configurado.
     *
     * @return Caminho padrão em String
     */
    String getCaminhoPadrao();
}

