package br.edu.pucminas.matricula.persistencia;

import br.edu.pucminas.matricula.App;
import br.edu.pucminas.matricula.enums.StatusDisciplina;
import br.edu.pucminas.matricula.enums.StatusMatricula;
import br.edu.pucminas.matricula.enums.TipoMatricula;
import br.edu.pucminas.matricula.model.*;
import br.edu.pucminas.matricula.service.SistemaCobranca;
import br.edu.pucminas.matricula.service.SistemaMatricula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suíte de testes unitários para o módulo de persistência de dados em arquivo TXT.
 * Valida a integridade do salvamento, carregamento e consistência relacional das entidades.
 */
class PersistenciaArquivoTxtTest {

    private Path tempFile;
    private PersistenciaDados persistencia;
    private SistemaMatricula sistemaOriginal;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("matricula_test_", ".txt");
        persistencia = new PersistenciaArquivoTxt(tempFile.toString());

        SistemaCobranca sistemaCobranca = new SistemaCobranca();
        sistemaOriginal = new SistemaMatricula(sistemaCobranca);
        App.popularDadosIniciais(sistemaOriginal);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempFile)) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Deve salvar e recarregar todo o estado do sistema mantendo integridade dos dados")
    void testSalvarECarregarSistemaCompleto() throws IOException {
        // Salva o sistema no arquivo TXT
        persistencia.salvar(sistemaOriginal);

        assertTrue(Files.exists(tempFile));
        assertTrue(Files.size(tempFile) > 0, "O arquivo TXT gerado não deve estar vazio");

        // Carrega em uma nova instância do sistema
        SistemaMatricula sistemaCarregado = persistencia.carregar();

        // 1. Validação de Cursos
        assertEquals(2, sistemaCarregado.getCursos().size());
        Curso bes = sistemaCarregado.buscarCurso("BES");
        assertNotNull(bes);
        assertEquals("Engenharia de Software", bes.getNome());
        assertEquals(240, bes.getTotalCreditos());
        assertEquals(6, bes.getGradeDisciplinas().size());

        // 2. Validação de Disciplinas
        Disciplina lds = sistemaCarregado.buscarDisciplina("LDS201");
        assertNotNull(lds);
        assertEquals("Laboratório de Desenvolvimento de Software", lds.getNome());
        assertEquals(4, lds.getCreditos());
        assertNotNull(lds.getProfessorResponsavel());
        assertEquals("PRF01", lds.getProfessorResponsavel().getId());

        // 3. Validação de Usuários (Professores, Alunos e Secretários)
        assertEquals(7, sistemaCarregado.getUsuarios().size());
        assertEquals(1, sistemaCarregado.getSecretarios().size());
        assertEquals(2, sistemaCarregado.getProfessores().size());
        assertEquals(4, sistemaCarregado.getAlunos().size());

        Professor profMilena = (Professor) sistemaCarregado.autenticarUsuario("milena@pucminas.br", "prof123");
        assertNotNull(profMilena);
        assertEquals("Profa. Milena Menezes", profMilena.getNome());
        assertEquals(2, profMilena.getDisciplinasLecionadas().size());

        Aluno aluno1 = (Aluno) sistemaCarregado.autenticarUsuario("fabricio@pucminas.br", "aluno123");
        assertNotNull(aluno1);
        assertEquals("Fabrício Lopes", aluno1.getNome());
        assertNotNull(aluno1.getCurso());
        assertEquals("BES", aluno1.getCurso().getCodigo());

        // 4. Validação de Currículo
        Curriculo cur = sistemaCarregado.buscarCurriculo("2026/2");
        assertNotNull(cur);
        assertTrue(cur.isPeriodoMatriculaAberto());
        assertEquals(6, cur.getDisciplinasOfertadas().size());

        // 5. Validação de Matrículas e Inscrições Cruzadas
        assertEquals(1, aluno1.getDisciplinasMatriculadas().size());
        assertEquals("LDS201", aluno1.getDisciplinasMatriculadas().get(0).getCodigo());

        List<Aluno> alunosLds = profMilena.consultarAlunos(lds);
        assertEquals(2, alunosLds.size());
        assertTrue(alunosLds.stream().anyMatch(a -> a.getId().equals("ALU01")));
        assertTrue(alunosLds.stream().anyMatch(a -> a.getId().equals("ALU02")));

        // 6. Validação de Cobranças
        List<Cobranca> cobrancasAluno1 = sistemaCarregado.getSistemaCobranca().consultarCobrancasPorAluno(aluno1);
        assertFalse(cobrancasAluno1.isEmpty());
        Cobranca cob = cobrancasAluno1.get(0);
        assertEquals("2026/2", cob.getSemestre());
        assertEquals(600.0, cob.getValorTotal(), 0.01);
    }

    @Test
    @DisplayName("Deve persistir alterações subsequentes como novas matrículas e cancelamentos")
    void testPersistirAlteracoesSubsequentes() throws IOException {
        Curriculo cur = sistemaOriginal.buscarCurriculo("2026/2");
        Aluno aluno3 = (Aluno) sistemaOriginal.autenticarUsuario("otavio@pucminas.br", "aluno123");
        Disciplina bd = sistemaOriginal.buscarDisciplina("BD101");

        // Realiza nova matrícula
        boolean matriculou = sistemaOriginal.solicitarMatricula(aluno3, bd, TipoMatricula.OBRIGATORIA, cur);
        assertTrue(matriculou);

        // Aluno 1 cancela matrícula em LDS201
        Aluno aluno1 = (Aluno) sistemaOriginal.autenticarUsuario("fabricio@pucminas.br", "aluno123");
        Disciplina lds = sistemaOriginal.buscarDisciplina("LDS201");
        boolean cancelou = sistemaOriginal.solicitarCancelamento(aluno1, lds, cur);
        assertTrue(cancelou);

        // Salva no arquivo TXT
        persistencia.salvar(sistemaOriginal);

        // Recarrega em um novo sistema
        SistemaMatricula sistemaRecarregado = persistencia.carregar();
        Aluno aluno3Rec = (Aluno) sistemaRecarregado.autenticarUsuario("otavio@pucminas.br", "aluno123");
        Aluno aluno1Rec = (Aluno) sistemaRecarregado.autenticarUsuario("fabricio@pucminas.br", "aluno123");
        Disciplina bdRec = sistemaRecarregado.buscarDisciplina("BD101");
        Disciplina ldsRec = sistemaRecarregado.buscarDisciplina("LDS201");

        // Aluno 3 deve estar matriculado em BD101
        assertTrue(aluno3Rec.getDisciplinasMatriculadas().contains(bdRec));

        // Aluno 1 não deve ter mais LDS201 nas matriculadas ativas
        assertFalse(aluno1Rec.getDisciplinasMatriculadas().contains(ldsRec));
        assertEquals(1, ldsRec.getQuantidadeInscritos(), "LDS201 deve ter apenas 1 aluno ativo agora");
    }

    @Test
    @DisplayName("Deve conter todas as seções esperadas no arquivo TXT estruturado")
    void testEstruturaArquivoTxt() throws IOException {
        persistencia.salvar(sistemaOriginal);

        List<String> linhas = Files.readAllLines(tempFile);
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[CURSOS]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[DISCIPLINAS]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[SECRETARIOS]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[PROFESSORES]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[ALUNOS]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[CURRICULOS]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[MATRICULAS]")));
        assertTrue(linhas.stream().anyMatch(l -> l.contains("[COBRANCAS]")));
    }

    @Test
    @DisplayName("Deve lançar PersistenciaException ao tentar carregar arquivo que não existe")
    void testArquivoNaoEncontrado() {
        PersistenciaDados pInvalida = new PersistenciaArquivoTxt("caminho_inexistente_12345.txt");
        assertThrows(PersistenciaException.class, pInvalida::carregar);
    }

    @Test
    @DisplayName("Deve reportar existência do arquivo de persistência corretamente")
    void testExisteArquivoPersistencia() throws IOException {
        Path tempPath = Files.createTempFile("teste_existencia_", ".txt");
        Files.deleteIfExists(tempPath);

        PersistenciaDados p = new PersistenciaArquivoTxt(tempPath.toString());
        assertFalse(p.existeArquivoPersistencia());

        p.salvar(sistemaOriginal);
        assertTrue(p.existeArquivoPersistencia());

        Files.deleteIfExists(tempPath);
    }
}

