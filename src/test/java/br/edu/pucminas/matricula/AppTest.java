package br.edu.pucminas.matricula;

import br.edu.pucminas.matricula.enums.StatusDisciplina;
import br.edu.pucminas.matricula.enums.StatusMatricula;
import br.edu.pucminas.matricula.enums.TipoMatricula;
import br.edu.pucminas.matricula.model.*;
import br.edu.pucminas.matricula.service.SistemaCobranca;
import br.edu.pucminas.matricula.service.SistemaMatricula;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suíte de testes unitários para o Sistema de Matrículas Universitário.
 * Valida a modelagem de classes, hierarquia, regras de negócio e stubs implementados na Sprint 02.
 */
class AppTest {

    private Curso curso;
    private Aluno aluno;
    private Professor professor;
    private Secretario secretario;
    private Curriculo curriculo;
    private SistemaCobranca sistemaCobranca;
    private SistemaMatricula sistemaMatricula;

    @BeforeEach
    void setUp() {
        curso = new Curso("ENG-SW", "Engenharia de Software", 240);
        aluno = new Aluno("A1", "Aluno Teste", "aluno@puc.br", "senha123", "MAT-001", curso);
        professor = new Professor("P1", "Professora Milena", "prof@puc.br", "prof123", "SIAPE-123");
        secretario = new Secretario("S1", "Secretaria Central", "sec@puc.br", "sec123", "Coordenador");

        curriculo = secretario.gerarCurriculoSemestre("2026/2");
        secretario.abrirPeriodoMatricula(curriculo);

        sistemaCobranca = new SistemaCobranca();
        sistemaMatricula = new SistemaMatricula(sistemaCobranca);

        sistemaMatricula.cadastrarUsuario(aluno);
        sistemaMatricula.cadastrarUsuario(professor);
        sistemaMatricula.cadastrarUsuario(secretario);
        sistemaMatricula.cadastrarCurso(curso);
        sistemaMatricula.cadastrarCurriculo(curriculo);
    }

    @Test
    @DisplayName("US01: Deve autenticar usuários corretamente por ID ou email e senha")
    void testAutenticacao() {
        assertTrue(aluno.autenticar("senha123"));
        assertFalse(aluno.autenticar("senhaErrada"));

        Usuario usuarioAutenticado = sistemaMatricula.autenticarUsuario("aluno@puc.br", "senha123");
        assertNotNull(usuarioAutenticado);
        assertEquals(aluno.getId(), usuarioAutenticado.getId());
    }

    @Test
    @DisplayName("US04: Deve respeitar limite de até 4 obrigatórias e até 2 optativas")
    void testLimitesMatricula() {
        // Tentar matricular em 4 obrigatórias (deve ter sucesso)
        for (int i = 1; i <= 4; i++) {
            Disciplina d = new Disciplina("OBR0" + i, "Obrigatoria " + i, 4);
            curriculo.adicionarOferta(d);
            boolean ok = sistemaMatricula.solicitarMatricula(aluno, d, TipoMatricula.OBRIGATORIA, curriculo);
            assertTrue(ok, "Deveria permitir a matrícula obrigatória " + i);
        }

        // Tentar matricular na 5ª obrigatória (deve falhar)
        Disciplina extraObr = new Disciplina("OBR05", "Obrigatoria 5", 4);
        curriculo.adicionarOferta(extraObr);
        boolean falhaObr = sistemaMatricula.solicitarMatricula(aluno, extraObr, TipoMatricula.OBRIGATORIA, curriculo);
        assertFalse(falhaObr, "Não deve permitir mais que 4 disciplinas obrigatórias");

        // Tentar matricular em 2 optativas (deve ter sucesso)
        for (int i = 1; i <= 2; i++) {
            Disciplina opt = new Disciplina("OPT0" + i, "Optativa " + i, 2);
            curriculo.adicionarOferta(opt);
            boolean ok = sistemaMatricula.solicitarMatricula(aluno, opt, TipoMatricula.OPTATIVA, curriculo);
            assertTrue(ok, "Deveria permitir a matrícula optativa " + i);
        }

        // Tentar matricular na 3ª optativa (deve falhar)
        Disciplina extraOpt = new Disciplina("OPT03", "Optativa 3", 2);
        curriculo.adicionarOferta(extraOpt);
        boolean falhaOpt = sistemaMatricula.solicitarMatricula(aluno, extraOpt, TipoMatricula.OPTATIVA, curriculo);
        assertFalse(falhaOpt, "Não deve permitir mais que 2 disciplinas optativas");

        assertEquals(6, aluno.getDisciplinasMatriculadas().size());
    }

    @Test
    @DisplayName("US05: Aluno deve conseguir cancelar matrícula dentro do período")
    void testCancelamentoMatricula() {
        Disciplina disc = new Disciplina("D100", "Algoritmos", 4);
        curriculo.adicionarOferta(disc);

        sistemaMatricula.solicitarMatricula(aluno, disc, TipoMatricula.OBRIGATORIA, curriculo);
        assertEquals(1, aluno.getDisciplinasMatriculadas().size());

        boolean cancelou = sistemaMatricula.solicitarCancelamento(aluno, disc, curriculo);
        assertTrue(cancelou);
        assertEquals(0, aluno.getDisciplinasMatriculadas().size());
    }

    @Test
    @DisplayName("US06: Regra de Quórum mínimo (3 alunos) ao encerrar período de matrículas")
    void testQuorumMinimo() {
        Disciplina comQuorum = new Disciplina("Q01", "Disciplina Com Quórum", 4);
        Disciplina semQuorum = new Disciplina("Q02", "Disciplina Sem Quórum", 4);
        curriculo.adicionarOferta(comQuorum);
        curriculo.adicionarOferta(semQuorum);

        Aluno a2 = new Aluno("A2", "Aluno 2", "a2@puc.br", "123", "M2", curso);
        Aluno a3 = new Aluno("A3", "Aluno 3", "a3@puc.br", "123", "M3", curso);

        // Matricular 3 alunos em comQuorum
        sistemaMatricula.solicitarMatricula(aluno, comQuorum, TipoMatricula.OBRIGATORIA, curriculo);
        sistemaMatricula.solicitarMatricula(a2, comQuorum, TipoMatricula.OBRIGATORIA, curriculo);
        sistemaMatricula.solicitarMatricula(a3, comQuorum, TipoMatricula.OBRIGATORIA, curriculo);

        // Matricular apenas 2 alunos em semQuorum
        sistemaMatricula.solicitarMatricula(aluno, semQuorum, TipoMatricula.OBRIGATORIA, curriculo);
        sistemaMatricula.solicitarMatricula(a2, semQuorum, TipoMatricula.OBRIGATORIA, curriculo);

        // Encerrar período
        secretario.fecharPeriodoMatricula(curriculo);

        assertEquals(StatusDisciplina.ATIVA, comQuorum.getStatus());
        assertEquals(StatusDisciplina.CANCELADA, semQuorum.getStatus());
    }

    @Test
    @DisplayName("US06: Limite de 60 alunos por disciplina encerra inscrições")
    void testCapacidadeMaximaDisciplina() {
        Disciplina lotada = new Disciplina("LOT01", "Turma Cheia", 4);
        curriculo.adicionarOferta(lotada);

        for (int i = 1; i <= 60; i++) {
            Aluno a = new Aluno("ID" + i, "Aluno " + i, "aluno" + i + "@puc.br", "123", "MAT" + i, curso);
            boolean ok = sistemaMatricula.solicitarMatricula(a, lotada, TipoMatricula.OBRIGATORIA, curriculo);
            assertTrue(ok);
        }

        assertEquals(StatusDisciplina.LOTADA, lotada.getStatus());
        assertFalse(lotada.temVagas());

        // 61º aluno deve ser recusado
        Aluno aluno61 = new Aluno("ID61", "Aluno 61", "a61@puc.br", "123", "MAT61", curso);
        boolean matriculou61 = sistemaMatricula.solicitarMatricula(aluno61, lotada, TipoMatricula.OBRIGATORIA, curriculo);
        assertFalse(matriculou61);
    }

    @Test
    @DisplayName("US07: Notificação e cálculo funcional do Sistema de Cobrança")
    void testSistemaCobrancaFuncional() {
        Disciplina d1 = new Disciplina("D1", "Prog I", 4);
        Disciplina d2 = new Disciplina("D2", "Banco de Dados", 4);
        curriculo.adicionarOferta(d1);
        curriculo.adicionarOferta(d2);

        sistemaMatricula.solicitarMatricula(aluno, d1, TipoMatricula.OBRIGATORIA, curriculo);
        sistemaMatricula.solicitarMatricula(aluno, d2, TipoMatricula.OBRIGATORIA, curriculo);

        List<Cobranca> cobrancas = sistemaCobranca.consultarCobrancasPorAluno(aluno);
        assertFalse(cobrancas.isEmpty());

        Cobranca ultimaCobranca = cobrancas.get(cobrancas.size() - 1);
        assertEquals("2026/2", ultimaCobranca.getSemestre());
        // 8 créditos * R$ 150 = R$ 1200.00
        assertEquals(1200.00, ultimaCobranca.getValorTotal(), 0.01);
    }

    @Test
    @DisplayName("US08: Professor consulta alunos matriculados na sua disciplina")
    void testProfessorConsultaAlunos() {
        Disciplina disc = new Disciplina("SW01", "Modelagem de Software", 4);
        professor.atribuirDisciplina(disc);
        curriculo.adicionarOferta(disc);

        sistemaMatricula.solicitarMatricula(aluno, disc, TipoMatricula.OBRIGATORIA, curriculo);

        List<Aluno> listaAlunos = professor.consultarAlunos(disc);
        assertEquals(1, listaAlunos.size());
        assertEquals("Aluno Teste", listaAlunos.get(0).getNome());
    }
}
