package br.edu.pucminas.matricula.view;

import br.edu.pucminas.matricula.enums.TipoMatricula;
import br.edu.pucminas.matricula.model.*;
import br.edu.pucminas.matricula.service.SistemaMatricula;

import java.util.List;
import java.util.Scanner;

/**
 * Interface de Linha de Comando (CLI) interativa para o Sistema de Matrículas.
 * Permite que Alunos, Professores e Secretários interajam com todas as funcionalidades do sistema.
 */
public class MenuConsole {
    private final SistemaMatricula sistemaMatricula;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public MenuConsole(SistemaMatricula sistemaMatricula) {
        this.sistemaMatricula = sistemaMatricula;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Inicia o loop principal do sistema.
     */
    public void iniciar() {
        boolean rodando = true;
        while (rodando) {
            if (usuarioLogado == null) {
                rodando = menuLogin();
            } else if (usuarioLogado instanceof Aluno aluno) {
                menuAluno(aluno);
            } else if (usuarioLogado instanceof Professor professor) {
                menuProfessor(professor);
            } else if (usuarioLogado instanceof Secretario secretario) {
                menuSecretaria(secretario);
            }
        }
        System.out.println("\n[SISTEMA] Programa encerrado. Obrigado por utilizar o Sistema de Matrículas PUC Minas!");
    }

    // ==========================================================
    // MENU PRINCIPAL / LOGIN
    // ==========================================================
    private boolean menuLogin() {
        System.out.println("\n========================================================");
        System.out.println("       SISTEMA DE MATRÍCULAS - PUC MINAS               ");
        System.out.println("========================================================");
        System.out.println("Contas de demonstração disponíveis:");
        System.out.println(" • Secretaria : secretaria@pucminas.br   (senha: admin123)");
        System.out.println(" • Professor  : milena@pucminas.br       (senha: prof123)");
        System.out.println(" • Aluno      : fabricio@pucminas.br     (senha: aluno123)");
        System.out.println(" • Aluno      : gabriel@pucminas.br      (senha: aluno123)");
        System.out.println("--------------------------------------------------------");
        System.out.println("1. Fazer Login");
        System.out.println("2. Listar Usuários Cadastrados");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");

        String opcao = scanner.nextLine().trim();
        switch (opcao) {
            case "1" -> realizarLogin();
            case "2" -> listarUsuarios();
            case "0" -> {
                return false;
            }
            default -> System.out.println("[!] Opção inválida. Tente novamente.");
        }
        return true;
    }

    private void realizarLogin() {
        System.out.print("\nDigite seu E-mail ou ID: ");
        String idOuEmail = scanner.nextLine().trim();
        System.out.print("Digite sua Senha: ");
        String senha = scanner.nextLine().trim();

        Usuario usuario = sistemaMatricula.autenticarUsuario(idOuEmail, senha);
        if (usuario != null) {
            this.usuarioLogado = usuario;
            System.out.println("\n[✓] Login realizado com sucesso! Bem-vindo(a), " + usuario.getNome() + "!");
        } else {
            System.out.println("\n[X] Credenciais incorretas. Verifique seu login e senha.");
        }
    }

    private void listarUsuarios() {
        System.out.println("\n--- USUÁRIOS CADASTRADOS NO SISTEMA ---");
        for (Usuario u : sistemaMatricula.getUsuarios()) {
            String tipo = (u instanceof Aluno) ? "Aluno" : (u instanceof Professor) ? "Professor" : "Secretaria";
            System.out.println(" • [" + tipo + "] " + u.getNome() + " | Email: " + u.getEmail() + " | ID: " + u.getId());
        }
    }

    // ==========================================================
    // MENU DO ALUNO
    // ==========================================================
    private void menuAluno(Aluno aluno) {
        System.out.println("\n========================================================");
        System.out.println("   PAINEL DO ALUNO: " + aluno.getNome() + " (" + aluno.getMatricula() + ")");
        System.out.println("   Curso: " + (aluno.getCurso() != null ? aluno.getCurso().getNome() : "Sem curso"));
        System.out.println("========================================================");
        System.out.println("1. Consultar Disciplinas Ofertadas no Semestre");
        System.out.println("2. Efetuar Matrícula em Disciplina (Obrigatória / Optativa)");
        System.out.println("3. Cancelar Matrícula em Disciplina");
        System.out.println("4. Ver Minhas Disciplinas Matriculadas e Créditos");
        System.out.println("5. Consultar Minhas Cobranças / Faturas");
        System.out.println("0. Fazer Logout");
        System.out.print("Escolha uma opção: ");

        String opcao = scanner.nextLine().trim();
        switch (opcao) {
            case "1" -> consultarDisciplinasOfertadas();
            case "2" -> matricularAluno(aluno);
            case "3" -> cancelarMatriculaAluno(aluno);
            case "4" -> verDisciplinasDoAluno(aluno);
            case "5" -> verCobrancasDoAluno(aluno);
            case "0" -> {
                System.out.println("[✓] Logout efetuado.");
                this.usuarioLogado = null;
            }
            default -> System.out.println("[!] Opção inválida.");
        }
    }

    private void consultarDisciplinasOfertadas() {
        Curriculo curriculo = sistemaMatricula.getCurriculoVigente();
        if (curriculo == null) {
            System.out.println("\n[!] Nenhum currículo cadastrado no momento.");
            return;
        }

        System.out.println("\n--- DISCIPLINAS OFERTADAS (" + curriculo.getSemestre() + ") ---");
        System.out.println("Período de Matrículas: " + (curriculo.isPeriodoMatriculaAberto() ? "ABERTO" : "FECHADO"));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-10s %-35s %-10s %-12s %-15s\n", "CÓDIGO", "NOME", "CRÉDITOS", "INSCRITOS", "STATUS");
        System.out.println("--------------------------------------------------------------------------------");

        for (Disciplina d : curriculo.getDisciplinasOfertadas()) {
            System.out.printf("%-10s %-35s %-10d %-12s %-15s\n",
                    d.getCodigo(),
                    d.getNome(),
                    d.getCreditos(),
                    d.getQuantidadeInscritos() + "/60",
                    d.getStatus());
        }
    }

    private void matricularAluno(Aluno aluno) {
        Curriculo curriculo = sistemaMatricula.getCurriculoVigente();
        if (curriculo == null || !curriculo.isPeriodoMatriculaAberto()) {
            System.out.println("\n[X] O período de matrículas está fechado no momento.");
            return;
        }

        consultarDisciplinasOfertadas();
        System.out.print("\nDigite o CÓDIGO da disciplina que deseja matricular: ");
        String codigo = scanner.nextLine().trim();

        Disciplina disciplina = sistemaMatricula.buscarDisciplina(codigo);
        if (disciplina == null) {
            System.out.println("[X] Disciplina não encontrada.");
            return;
        }

        System.out.println("Escolha o tipo de matrícula:");
        System.out.println("1. Obrigatória (1ª Opção - limite de até 4)");
        System.out.println("2. Optativa (Alternativa - limite de até 2)");
        System.out.print("Opção: ");
        String tipoOpcao = scanner.nextLine().trim();

        TipoMatricula tipo = tipoOpcao.equals("2") ? TipoMatricula.OPTATIVA : TipoMatricula.OBRIGATORIA;

        boolean sucesso = sistemaMatricula.solicitarMatricula(aluno, disciplina, tipo, curriculo);
        if (sucesso) {
            System.out.println("\n[✓] Matrícula realizada com sucesso em " + disciplina.getNome() + " (" + tipo.getDescricao() + ")!");
        } else {
            System.out.println("\n[X] Não foi possível efetuar a matrícula.");
            System.out.println("Possíveis motivos: limite atingido (máx 4 obrigatórias / 2 optativas), turma lotada (60 alunos) ou aluno já matriculado.");
        }
    }

    private void cancelarMatriculaAluno(Aluno aluno) {
        Curriculo curriculo = sistemaMatricula.getCurriculoVigente();
        if (curriculo == null || !curriculo.isPeriodoMatriculaAberto()) {
            System.out.println("\n[X] O período de matrículas está fechado para alterações.");
            return;
        }

        List<Disciplina> matriculadas = aluno.getDisciplinasMatriculadas();
        if (matriculadas.isEmpty()) {
            System.out.println("\n[!] Você não possui nenhuma matrícula ativa para cancelar.");
            return;
        }

        System.out.println("\nSuas disciplinas ativas:");
        for (Disciplina d : matriculadas) {
            System.out.println(" • [" + d.getCodigo() + "] " + d.getNome());
        }

        System.out.print("\nDigite o CÓDIGO da disciplina que deseja cancelar: ");
        String codigo = scanner.nextLine().trim();
        Disciplina disciplina = sistemaMatricula.buscarDisciplina(codigo);

        if (disciplina == null) {
            System.out.println("[X] Disciplina não encontrada.");
            return;
        }

        boolean cancelou = sistemaMatricula.solicitarCancelamento(aluno, disciplina, curriculo);
        if (cancelou) {
            System.out.println("[✓] Matrícula cancelada com sucesso!");
        } else {
            System.out.println("[X] Não foi possível cancelar a matrícula.");
        }
    }

    private void verDisciplinasDoAluno(Aluno aluno) {
        System.out.println("\n--- MINHAS DISCIPLINAS MATRICULADAS ---");
        List<Disciplina> matriculadas = aluno.getDisciplinasMatriculadas();
        if (matriculadas.isEmpty()) {
            System.out.println("Nenhuma disciplina matriculada no momento.");
        } else {
            int totalCreditos = 0;
            for (Disciplina d : matriculadas) {
                totalCreditos += d.getCreditos();
                System.out.println(" • [" + d.getCodigo() + "] " + d.getNome() + " (" + d.getCreditos() + " créditos)");
            }
            System.out.println("---------------------------------------");
            System.out.println("Total de créditos cursados: " + totalCreditos);
            System.out.println("Obrigatórias ativas: " + aluno.getQuantidadeMatriculasPorTipo(TipoMatricula.OBRIGATORIA) + "/4");
            System.out.println("Optativas ativas: " + aluno.getQuantidadeMatriculasPorTipo(TipoMatricula.OPTATIVA) + "/2");
        }
    }

    private void verCobrancasDoAluno(Aluno aluno) {
        System.out.println("\n--- MINHAS COBRANÇAS FINANCEIRAS ---");
        var cobrancas = sistemaMatricula.getSistemaCobranca().consultarCobrancasPorAluno(aluno);
        if (cobrancas.isEmpty()) {
            System.out.println("Nenhuma fatura emitida até o momento.");
        } else {
            for (Cobranca c : cobrancas) {
                System.out.println(" • Fatura ID: " + c.getId().substring(0, 8) + " | Semestre: " + c.getSemestre() +
                        " | Valor: R$ " + String.format("%.2f", c.getValorTotal()) +
                        " | Data: " + c.getDataEmissao().toLocalDate() +
                        " | Status: " + (c.isPaga() ? "PAGA" : "EM ABERTO"));
            }
        }
    }

    // ==========================================================
    // MENU DO PROFESSOR
    // ==========================================================
    private void menuProfessor(Professor professor) {
        System.out.println("\n========================================================");
        System.out.println("   PAINEL DO PROFESSOR: " + professor.getNome() + " (SIAPE: " + professor.getSiape() + ")");
        System.out.println("========================================================");
        System.out.println("1. Listar Minhas Disciplinas Lecionadas");
        System.out.println("2. Consultar Alunos Matriculados em uma Disciplina");
        System.out.println("3. Assumir / Atribuir Nova Disciplina Ofertada");
        System.out.println("0. Fazer Logout");
        System.out.print("Escolha uma opção: ");

        String opcao = scanner.nextLine().trim();
        switch (opcao) {
            case "1" -> listarDisciplinasProfessor(professor);
            case "2" -> consultarAlunosDisciplina(professor);
            case "3" -> atribuirDisciplinaProfessor(professor);
            case "0" -> {
                System.out.println("[✓] Logout efetuado.");
                this.usuarioLogado = null;
            }
            default -> System.out.println("[!] Opção inválida.");
        }
    }

    private void listarDisciplinasProfessor(Professor professor) {
        System.out.println("\n--- MINHAS DISCIPLINAS ---");
        List<Disciplina> lista = professor.getDisciplinasLecionadas();
        if (lista.isEmpty()) {
            System.out.println("Você ainda não está vinculado a nenhuma disciplina.");
        } else {
            for (Disciplina d : lista) {
                System.out.println(" • [" + d.getCodigo() + "] " + d.getNome() + " (" + d.getQuantidadeInscritos() + " inscritos | Status: " + d.getStatus() + ")");
            }
        }
    }

    private void consultarAlunosDisciplina(Professor professor) {
        listarDisciplinasProfessor(professor);
        if (professor.getDisciplinasLecionadas().isEmpty()) return;

        System.out.print("\nDigite o CÓDIGO da disciplina para consultar os alunos: ");
        String codigo = scanner.nextLine().trim();
        Disciplina d = sistemaMatricula.buscarDisciplina(codigo);

        if (d == null) {
            System.out.println("[X] Disciplina não encontrada.");
            return;
        }

        List<Aluno> alunos = professor.consultarAlunos(d);
        System.out.println("\n--- ALUNOS MATRICULADOS EM: " + d.getNome() + " (" + alunos.size() + " alunos) ---");
        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno matriculado nesta disciplina.");
        } else {
            for (Aluno a : alunos) {
                System.out.println(" • " + a.getNome() + " | Matrícula: " + a.getMatricula() + " | Curso: " + (a.getCurso() != null ? a.getCurso().getNome() : "N/A"));
            }
        }
    }

    private void atribuirDisciplinaProfessor(Professor professor) {
        Curriculo curr = sistemaMatricula.getCurriculoVigente();
        if (curr == null) {
            System.out.println("[!] Nenhum currículo ativo.");
            return;
        }

        System.out.println("\nDisciplinas ofertadas no semestre " + curr.getSemestre() + ":");
        for (Disciplina d : curr.getDisciplinasOfertadas()) {
            String prof = (d.getProfessorResponsavel() != null) ? d.getProfessorResponsavel().getNome() : "Sem professor";
            System.out.println(" • [" + d.getCodigo() + "] " + d.getNome() + " (Docente: " + prof + ")");
        }

        System.out.print("\nDigite o CÓDIGO da disciplina que deseja assumir: ");
        String cod = scanner.nextLine().trim();
        Disciplina d = sistemaMatricula.buscarDisciplina(cod);
        if (d != null) {
            professor.atribuirDisciplina(d);
            System.out.println("[✓] Disciplina " + d.getNome() + " atribuída com sucesso ao seu perfil!");
        } else {
            System.out.println("[X] Disciplina não encontrada.");
        }
    }

    // ==========================================================
    // MENU DA SECRETARIA
    // ==========================================================
    private void menuSecretaria(Secretario secretario) {
        System.out.println("\n========================================================");
        System.out.println("   PAINEL DA SECRETARIA: " + secretario.getNome() + " (" + secretario.getCargo() + ")");
        System.out.println("========================================================");
        System.out.println("1. Gerar Currículo para Novo Semestre");
        System.out.println("2. Adicionar Oferta de Disciplina ao Currículo");
        System.out.println("3. Abrir Período de Matrículas");
        System.out.println("4. Fechar Período de Matrículas (Verificar Quórum Mínimo)");
        System.out.println("5. Cadastrar Novo Curso");
        System.out.println("6. Cadastrar Nova Disciplina");
        System.out.println("7. Cadastrar Novo Aluno");
        System.out.println("8. Cadastrar Novo Professor");
        System.out.println("9. Listar Visão Geral do Sistema");
        System.out.println("0. Fazer Logout");
        System.out.print("Escolha uma opção: ");

        String opcao = scanner.nextLine().trim();
        switch (opcao) {
            case "1" -> gerarCurriculo(secretario);
            case "2" -> ofertarDisciplinaNoCurriculo();
            case "3" -> abrirPeriodo(secretario);
            case "4" -> fecharPeriodo(secretario);
            case "5" -> cadastrarCurso();
            case "6" -> cadastrarDisciplina();
            case "7" -> cadastrarAluno();
            case "8" -> cadastrarProfessor();
            case "9" -> listarVisaoGeral();
            case "0" -> {
                System.out.println("[✓] Logout efetuado.");
                this.usuarioLogado = null;
            }
            default -> System.out.println("[!] Opção inválida.");
        }
    }

    private void gerarCurriculo(Secretario secretario) {
        System.out.print("\nDigite o identificador do semestre (ex: 2026/2): ");
        String semestre = scanner.nextLine().trim();

        if (sistemaMatricula.buscarCurriculo(semestre) != null) {
            System.out.println("[!] Já existe currículo cadastrado para o semestre " + semestre);
            return;
        }

        Curriculo novo = secretario.gerarCurriculoSemestre(semestre);
        sistemaMatricula.cadastrarCurriculo(novo);
        System.out.println("[✓] Currículo gerado com sucesso para o semestre " + semestre + "!");
    }

    private void ofertarDisciplinaNoCurriculo() {
        Curriculo curr = sistemaMatricula.getCurriculoVigente();
        if (curr == null) {
            System.out.println("[!] Não há currículo cadastrado. Crie um currículo primeiro.");
            return;
        }

        System.out.print("Digite o CÓDIGO da disciplina existente que deseja ofertar no semestre " + curr.getSemestre() + ": ");
        String cod = scanner.nextLine().trim();
        Disciplina d = sistemaMatricula.buscarDisciplina(cod);

        if (d != null) {
            curr.adicionarOferta(d);
            System.out.println("[✓] Disciplina " + d.getNome() + " adicionada à oferta do semestre " + curr.getSemestre() + "!");
        } else {
            System.out.println("[X] Disciplina com código '" + cod + "' não encontrada.");
        }
    }

    private void abrirPeriodo(Secretario secretario) {
        Curriculo curr = sistemaMatricula.getCurriculoVigente();
        if (curr == null) {
            System.out.println("[!] Crie um currículo antes de abrir o período de matrículas.");
            return;
        }
        secretario.abrirPeriodoMatricula(curr);
        System.out.println("[✓] Período de matrículas ABERTO com sucesso para o semestre " + curr.getSemestre() + "!");
    }

    private void fecharPeriodo(Secretario secretario) {
        Curriculo curr = sistemaMatricula.getCurriculoVigente();
        if (curr == null) {
            System.out.println("[!] Nenhum currículo ativo.");
            return;
        }

        System.out.println("\n[SISTEMA] Fechando período de matrículas e processando quórum de viabilidade...");
        secretario.fecharPeriodoMatricula(curr);

        System.out.println("\n--- RESULTADO DA VERIFICAÇÃO DE QUÓRUM (MÍNIMO 3 ALUNOS) ---");
        for (Disciplina d : curr.getDisciplinasOfertadas()) {
            System.out.println(" • [" + d.getCodigo() + "] " + d.getNome() + " | Alunos: " + d.getQuantidadeInscritos() + " | Status: " + d.getStatus());
        }
    }

    private void cadastrarCurso() {
        System.out.print("\nCódigo do Curso (ex: CC): ");
        String cod = scanner.nextLine().trim();
        System.out.print("Nome do Curso (ex: Ciência da Computação): ");
        String nome = scanner.nextLine().trim();
        System.out.print("Total de Créditos (ex: 200): ");
        try {
            int creditos = Integer.parseInt(scanner.nextLine().trim());
            Curso curso = new Curso(cod, nome, creditos);
            sistemaMatricula.cadastrarCurso(curso);
            System.out.println("[✓] Curso " + nome + " cadastrado com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("[X] Quantidade de créditos inválida.");
        }
    }

    private void cadastrarDisciplina() {
        System.out.print("\nCódigo da Disciplina (ex: BD101): ");
        String cod = scanner.nextLine().trim();
        System.out.print("Nome da Disciplina: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Quantidade de Créditos: ");
        try {
            int creditos = Integer.parseInt(scanner.nextLine().trim());
            Disciplina d = new Disciplina(cod, nome, creditos);

            System.out.print("Código do Curso ao qual pertence (deixe vazio se nenhum): ");
            String codCurso = scanner.nextLine().trim();
            if (!codCurso.isEmpty()) {
                Curso curso = sistemaMatricula.buscarCurso(codCurso);
                if (curso != null) {
                    curso.adicionarDisciplina(d);
                    System.out.println("[✓] Disciplina associada ao curso " + curso.getNome());
                } else {
                    System.out.println("[!] Curso não encontrado. Disciplina criada de forma avulsa.");
                }
            }

            Curriculo curr = sistemaMatricula.getCurriculoVigente();
            if (curr != null) {
                curr.adicionarOferta(d);
                System.out.println("[✓] Disciplina adicionada à oferta do currículo " + curr.getSemestre());
            }
            System.out.println("[✓] Disciplina " + nome + " cadastrada com sucesso!");
        } catch (NumberFormatException e) {
            System.out.println("[X] Valor de créditos inválido.");
        }
    }

    private void cadastrarAluno() {
        System.out.print("\nNome do Aluno: ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();
        System.out.print("Número de Matrícula: ");
        String matricula = scanner.nextLine().trim();

        System.out.print("Código do Curso do Aluno: ");
        String codCurso = scanner.nextLine().trim();
        Curso curso = sistemaMatricula.buscarCurso(codCurso);

        String id = "ALU" + (sistemaMatricula.getAlunos().size() + 1);
        Aluno novoAluno = new Aluno(id, nome, email, senha, matricula, curso);
        sistemaMatricula.cadastrarUsuario(novoAluno);
        System.out.println("[✓] Aluno " + nome + " cadastrado com sucesso! ID: " + id);
    }

    private void cadastrarProfessor() {
        System.out.print("\nNome do Professor: ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();
        System.out.print("SIAPE / Registro do Docente: ");
        String siape = scanner.nextLine().trim();

        String id = "PRF" + (sistemaMatricula.getProfessores().size() + 1);
        Professor prof = new Professor(id, nome, email, senha, siape);
        sistemaMatricula.cadastrarUsuario(prof);
        System.out.println("[✓] Professor(a) " + nome + " cadastrado(a) com sucesso! ID: " + id);
    }

    private void listarVisaoGeral() {
        System.out.println("\n========================================================");
        System.out.println("               VISÃO GERAL DO SISTEMA                   ");
        System.out.println("========================================================");

        System.out.println("\n[ CURSOS CADASTRADOS (" + sistemaMatricula.getCursos().size() + ") ]");
        for (Curso c : sistemaMatricula.getCursos()) {
            System.out.println(" • " + c.getNome() + " [" + c.getCodigo() + "] - " + c.getTotalCreditos() + " créditos");
        }

        System.out.println("\n[ CURRÍCULOS SEMESTRAIS (" + sistemaMatricula.getCurriculos().size() + ") ]");
        for (Curriculo cur : sistemaMatricula.getCurriculos()) {
            System.out.println(" • Semestre " + cur.getSemestre() + " | Período de Matrículas: " + (cur.isPeriodoMatriculaAberto() ? "ABERTO" : "FECHADO") + " | " + cur.getDisciplinasOfertadas().size() + " ofertas");
        }

        System.out.println("\n[ TOTAL DE USUÁRIOS ]");
        System.out.println(" • Alunos: " + sistemaMatricula.getAlunos().size());
        System.out.println(" • Professores: " + sistemaMatricula.getProfessores().size());
        System.out.println(" • Secretaria: " + sistemaMatricula.getSecretarios().size());
    }
}
