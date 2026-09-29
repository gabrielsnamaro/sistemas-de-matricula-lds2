package br.edu.pucminas.matricula;

import br.edu.pucminas.matricula.enums.TipoMatricula;
import br.edu.pucminas.matricula.model.*;
import br.edu.pucminas.matricula.persistencia.PersistenciaArquivoTxt;
import br.edu.pucminas.matricula.persistencia.PersistenciaDados;
import br.edu.pucminas.matricula.service.SistemaCobranca;
import br.edu.pucminas.matricula.service.SistemaMatricula;
import br.edu.pucminas.matricula.view.MenuConsole;

import java.io.IOException;

/**
 * Ponto de entrada do Sistema de Matrículas Universitário.
 * Inicializa o módulo de persistência de dados em arquivo TXT e dispara o menu interativo via console.
 */
public class App {
    public static void main(String[] args) {
        // 1. Inicializa o serviço de cobrança, controlador de matrículas e módulo de persistência
        SistemaCobranca sistemaCobranca = new SistemaCobranca();
        SistemaMatricula sistemaMatricula = new SistemaMatricula(sistemaCobranca);
        PersistenciaDados persistencia = new PersistenciaArquivoTxt();

        // 2. Se o arquivo TXT de persistência existir, carrega os dados; caso contrário, inicializa dados demo e persiste
        if (persistencia.existeArquivoPersistencia()) {
            try {
                persistencia.carregar(sistemaMatricula);
                System.out.println("[PERSISTÊNCIA] Dados carregados com sucesso do arquivo " + persistencia.getCaminhoPadrao());
            } catch (IOException e) {
                System.err.println("[PERSISTÊNCIA] Erro ao carregar dados do arquivo: " + e.getMessage());
                System.out.println("[PERSISTÊNCIA] Inicializando dados de demonstração em memória...");
                popularDadosIniciais(sistemaMatricula);
                try {
                    persistencia.salvar(sistemaMatricula);
                } catch (IOException ex) {
                    System.err.println("[PERSISTÊNCIA] Falha ao persistir dados iniciais: " + ex.getMessage());
                }
            }
        } else {
            System.out.println("[PERSISTÊNCIA] Arquivo TXT não encontrado. Inicializando base de demonstração e persistindo...");
            popularDadosIniciais(sistemaMatricula);
            try {
                persistencia.salvar(sistemaMatricula);
                System.out.println("[PERSISTÊNCIA] Dados de demonstração salvos com sucesso em " + persistencia.getCaminhoPadrao());
            } catch (IOException e) {
                System.err.println("[PERSISTÊNCIA] Falha ao persistir dados iniciais: " + e.getMessage());
            }
        }

        // 3. Inicia o menu interativo no terminal
        MenuConsole menu = new MenuConsole(sistemaMatricula, persistencia);
        menu.iniciar();
    }

    /**
     * Popula o sistema com dados iniciais para viabilizar testes imediatos.
     */
    public static void popularDadosIniciais(SistemaMatricula sistema) {
        // Cursos
        Curso engenhariaSoftware = new Curso("BES", "Engenharia de Software", 240);
        Curso cienciaComputacao = new Curso("BCC", "Ciência da Computação", 240);

        // Disciplinas
        Disciplina d1 = new Disciplina("LDS201", "Laboratório de Desenvolvimento de Software", 4);
        Disciplina d2 = new Disciplina("REQ202", "Engenharia de Requisitos", 4);
        Disciplina d3 = new Disciplina("ARQ203", "Arquitetura de Software", 4);
        Disciplina d4 = new Disciplina("BD101", "Banco de Dados Relacional", 4);
        Disciplina d5 = new Disciplina("OPT01", "Inteligência Artificial Aplicada", 2);
        Disciplina d6 = new Disciplina("OPT02", "Segurança da Informação", 2);

        engenhariaSoftware.adicionarDisciplina(d1);
        engenhariaSoftware.adicionarDisciplina(d2);
        engenhariaSoftware.adicionarDisciplina(d3);
        engenhariaSoftware.adicionarDisciplina(d4);
        engenhariaSoftware.adicionarDisciplina(d5);
        engenhariaSoftware.adicionarDisciplina(d6);

        sistema.cadastrarCurso(engenhariaSoftware);
        sistema.cadastrarCurso(cienciaComputacao);

        // Usuários
        Secretario secretaria = new Secretario("SEC01", "Ana Maria (Secretaria)", "secretaria@pucminas.br", "admin123", "Coordenadora de Matrículas");
        Professor profMilena = new Professor("PRF01", "Profa. Milena Menezes", "milena@pucminas.br", "prof123", "SIAPE-9876");
        Professor profRoberto = new Professor("PRF02", "Prof. Roberto Silva", "roberto@pucminas.br", "prof123", "SIAPE-5432");

        profMilena.atribuirDisciplina(d1);
        profMilena.atribuirDisciplina(d3);
        profRoberto.atribuirDisciplina(d2);
        profRoberto.atribuirDisciplina(d4);

        Aluno aluno1 = new Aluno("ALU01", "Fabrício Lopes", "fabricio@pucminas.br", "aluno123", "MAT-202601", engenhariaSoftware);
        Aluno aluno2 = new Aluno("ALU02", "Gabriel Amaro", "gabriel@pucminas.br", "aluno123", "MAT-202602", engenhariaSoftware);
        Aluno aluno3 = new Aluno("ALU03", "Otávio Silva", "otavio@pucminas.br", "aluno123", "MAT-202603", engenhariaSoftware);
        Aluno aluno4 = new Aluno("ALU04", "Samuel Rebula", "samuel@pucminas.br", "aluno123", "MAT-202604", engenhariaSoftware);

        sistema.cadastrarUsuario(secretaria);
        sistema.cadastrarUsuario(profMilena);
        sistema.cadastrarUsuario(profRoberto);
        sistema.cadastrarUsuario(aluno1);
        sistema.cadastrarUsuario(aluno2);
        sistema.cadastrarUsuario(aluno3);
        sistema.cadastrarUsuario(aluno4);

        // Currículo Semestral
        Curriculo sem2026_2 = secretaria.gerarCurriculoSemestre("2026/2");
        sem2026_2.adicionarOferta(d1);
        sem2026_2.adicionarOferta(d2);
        sem2026_2.adicionarOferta(d3);
        sem2026_2.adicionarOferta(d4);
        sem2026_2.adicionarOferta(d5);
        sem2026_2.adicionarOferta(d6);

        // Período aberto para início imediato
        secretaria.abrirPeriodoMatricula(sem2026_2);
        sistema.cadastrarCurriculo(sem2026_2);

        // Matrículas prévias para demonstrar turmas com alunos inscritos
        sistema.solicitarMatricula(aluno1, d1, TipoMatricula.OBRIGATORIA, sem2026_2);
        sistema.solicitarMatricula(aluno2, d1, TipoMatricula.OBRIGATORIA, sem2026_2);
    }
}
