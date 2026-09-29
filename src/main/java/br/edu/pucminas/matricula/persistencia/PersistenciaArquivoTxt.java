package br.edu.pucminas.matricula.persistencia;

import br.edu.pucminas.matricula.enums.StatusDisciplina;
import br.edu.pucminas.matricula.enums.StatusMatricula;
import br.edu.pucminas.matricula.enums.TipoMatricula;
import br.edu.pucminas.matricula.model.*;
import br.edu.pucminas.matricula.service.SistemaCobranca;
import br.edu.pucminas.matricula.service.SistemaMatricula;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do mecanismo de persistência de dados em um (1) único arquivo TXT.
 * Salva e recupera todas as entidades do sistema (Cursos, Disciplinas, Usuários,
 * Currículos, Matrículas e Cobranças) mantendo a integridade referencial dos objetos.
 */
public class PersistenciaArquivoTxt implements PersistenciaDados {

    public static final String ARQUIVO_PADRAO = "dados_sistema.txt";

    private static final String SECAO_CURSOS = "[CURSOS]";
    private static final String SECAO_DISCIPLINAS = "[DISCIPLINAS]";
    private static final String SECAO_SECRETARIOS = "[SECRETARIOS]";
    private static final String SECAO_PROFESSORES = "[PROFESSORES]";
    private static final String SECAO_ALUNOS = "[ALUNOS]";
    private static final String SECAO_CURRICULOS = "[CURRICULOS]";
    private static final String SECAO_MATRICULAS = "[MATRICULAS]";
    private static final String SECAO_COBRANCAS = "[COBRANCAS]";

    private final String caminhoPadrao;

    public PersistenciaArquivoTxt() {
        this(ARQUIVO_PADRAO);
    }

    public PersistenciaArquivoTxt(String caminhoPadrao) {
        this.caminhoPadrao = caminhoPadrao != null && !caminhoPadrao.isBlank() ? caminhoPadrao.trim() : ARQUIVO_PADRAO;
    }

    @Override
    public void salvar(SistemaMatricula sistema) throws IOException {
        salvar(sistema, this.caminhoPadrao);
    }

    @Override
    public void salvar(SistemaMatricula sistema, String caminhoArquivo) throws IOException {
        if (sistema == null) {
            throw new IllegalArgumentException("A instância do SistemaMatricula não pode ser nula.");
        }
        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            caminhoArquivo = this.caminhoPadrao;
        }

        Path path = Paths.get(caminhoArquivo);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        // Coleta todas as disciplinas do sistema garantindo unicidade
        Map<String, Disciplina> todasDisciplinas = coletarTodasDisciplinas(sistema);

        // Coleta todas as matrículas do sistema garantindo unicidade
        Map<String, Matricula> todasMatriculas = coletarTodasMatriculas(sistema, todasDisciplinas);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write("# ====================================================================\n");
            writer.write("# SISTEMA DE MATRÍCULAS UNIVERSITÁRIO - ARQUIVO DE PERSISTÊNCIA (TXT)\n");
            writer.write("# Gerado em: " + LocalDateTime.now() + "\n");
            writer.write("# ====================================================================\n\n");

            // 1. Cursos
            writer.write(SECAO_CURSOS + "\n");
            writer.write("# CODIGO;NOME;TOTAL_CREDITOS;DISCIPLINAS_CODIGOS\n");
            for (Curso c : sistema.getCursos()) {
                String disciplinasStr = c.getGradeDisciplinas().stream()
                        .map(Disciplina::getCodigo)
                        .collect(Collectors.joining(","));
                writer.write(String.format("%s;%s;%d;%s\n",
                        escapar(c.getCodigo()),
                        escapar(c.getNome()),
                        c.getTotalCreditos(),
                        disciplinasStr));
            }
            writer.write("\n");

            // 2. Disciplinas
            writer.write(SECAO_DISCIPLINAS + "\n");
            writer.write("# CODIGO;NOME;CREDITOS;STATUS;ID_PROFESSOR_RESPONSAVEL\n");
            for (Disciplina d : todasDisciplinas.values()) {
                String profId = d.getProfessorResponsavel() != null ? d.getProfessorResponsavel().getId() : "";
                writer.write(String.format("%s;%s;%d;%s;%s\n",
                        escapar(d.getCodigo()),
                        escapar(d.getNome()),
                        d.getCreditos(),
                        d.getStatus().name(),
                        escapar(profId)));
            }
            writer.write("\n");

            // 3. Secretários
            writer.write(SECAO_SECRETARIOS + "\n");
            writer.write("# ID;NOME;EMAIL;SENHA;CARGO\n");
            for (Secretario s : sistema.getSecretarios()) {
                writer.write(String.format("%s;%s;%s;%s;%s\n",
                        escapar(s.getId()),
                        escapar(s.getNome()),
                        escapar(s.getEmail()),
                        escapar(s.getSenha()),
                        escapar(s.getCargo())));
            }
            writer.write("\n");

            // 4. Professores
            writer.write(SECAO_PROFESSORES + "\n");
            writer.write("# ID;NOME;EMAIL;SENHA;SIAPE;DISCIPLINAS_LECIONADAS\n");
            for (Professor p : sistema.getProfessores()) {
                String lecionadasStr = p.getDisciplinasLecionadas().stream()
                        .map(Disciplina::getCodigo)
                        .collect(Collectors.joining(","));
                writer.write(String.format("%s;%s;%s;%s;%s;%s\n",
                        escapar(p.getId()),
                        escapar(p.getNome()),
                        escapar(p.getEmail()),
                        escapar(p.getSenha()),
                        escapar(p.getSiape()),
                        lecionadasStr));
            }
            writer.write("\n");

            // 5. Alunos
            writer.write(SECAO_ALUNOS + "\n");
            writer.write("# ID;NOME;EMAIL;SENHA;MATRICULA;CODIGO_CURSO\n");
            for (Aluno a : sistema.getAlunos()) {
                String codCurso = a.getCurso() != null ? a.getCurso().getCodigo() : "";
                writer.write(String.format("%s;%s;%s;%s;%s;%s\n",
                        escapar(a.getId()),
                        escapar(a.getNome()),
                        escapar(a.getEmail()),
                        escapar(a.getSenha()),
                        escapar(a.getMatricula()),
                        escapar(codCurso)));
            }
            writer.write("\n");

            // 6. Currículos
            writer.write(SECAO_CURRICULOS + "\n");
            writer.write("# SEMESTRE;PERIODO_ABERTO;DISCIPLINAS_OFERTADAS\n");
            for (Curriculo cur : sistema.getCurriculos()) {
                String ofertadasStr = cur.getDisciplinasOfertadas().stream()
                        .map(Disciplina::getCodigo)
                        .collect(Collectors.joining(","));
                writer.write(String.format("%s;%b;%s\n",
                        escapar(cur.getSemestre()),
                        cur.isPeriodoMatriculaAberto(),
                        ofertadasStr));
            }
            writer.write("\n");

            // 7. Matrículas
            writer.write(SECAO_MATRICULAS + "\n");
            writer.write("# ID;ID_ALUNO;CODIGO_DISCIPLINA;TIPO;STATUS;DATA_MATRICULA\n");
            for (Matricula m : todasMatriculas.values()) {
                String idAluno = m.getAluno() != null ? m.getAluno().getId() : "";
                String codDisc = m.getDisciplina() != null ? m.getDisciplina().getCodigo() : "";
                String dataStr = m.getDataMatricula() != null ? m.getDataMatricula().toString() : LocalDateTime.now().toString();
                writer.write(String.format("%s;%s;%s;%s;%s;%s\n",
                        escapar(m.getId()),
                        escapar(idAluno),
                        escapar(codDisc),
                        m.getTipo().name(),
                        m.getStatus().name(),
                        dataStr));
            }
            writer.write("\n");

            // 8. Cobranças
            writer.write(SECAO_COBRANCAS + "\n");
            writer.write("# ID;ID_ALUNO;SEMESTRE;VALOR_TOTAL;DATA_EMISSAO;PAGA;IDS_MATRICULAS\n");
            for (Cobranca cob : sistema.getSistemaCobranca().getCobrancasEmitidas()) {
                String idAluno = cob.getAluno() != null ? cob.getAluno().getId() : "";
                String dataCobStr = cob.getDataEmissao() != null ? cob.getDataEmissao().toString() : LocalDateTime.now().toString();
                String matsIds = cob.getMatriculasCobradas().stream()
                        .map(Matricula::getId)
                        .collect(Collectors.joining(","));
                writer.write(String.format(Locale.US, "%s;%s;%s;%.2f;%s;%b;%s\n",
                        escapar(cob.getId()),
                        escapar(idAluno),
                        escapar(cob.getSemestre()),
                        cob.getValorTotal(),
                        dataCobStr,
                        cob.isPaga(),
                        matsIds));
            }
        }
    }

    @Override
    public void carregar(SistemaMatricula sistema) throws IOException {
        carregar(sistema, this.caminhoPadrao);
    }

    @Override
    public void carregar(SistemaMatricula sistema, String caminhoArquivo) throws IOException {
        if (sistema == null) {
            throw new IllegalArgumentException("A instância do SistemaMatricula não pode ser nula.");
        }
        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            caminhoArquivo = this.caminhoPadrao;
        }

        Path path = Paths.get(caminhoArquivo);
        if (!Files.exists(path)) {
            throw new PersistenciaException("Arquivo de persistência não encontrado: " + caminhoArquivo);
        }

        // Lê e separa o arquivo em blocos de seções
        Map<String, List<String>> secoes = lerSecoes(path);

        // Limpa o estado atual do sistema para evitar duplicações
        sistema.limparDados();

        // 1. Carrega Disciplinas
        Map<String, Disciplina> disciplinasMap = new LinkedHashMap<>();
        Map<String, String> profIdPorDisciplina = new LinkedHashMap<>();
        if (secoes.containsKey(SECAO_DISCIPLINAS)) {
            for (String linha : secoes.get(SECAO_DISCIPLINAS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 4) {
                    String codigo = desescapar(partes[0].trim());
                    String nome = desescapar(partes[1].trim());
                    int creditos = Integer.parseInt(partes[2].trim());
                    StatusDisciplina status = StatusDisciplina.valueOf(partes[3].trim());
                    String idProf = partes.length >= 5 ? desescapar(partes[4].trim()) : "";

                    Disciplina d = new Disciplina(codigo, nome, creditos);
                    d.setStatus(status);
                    disciplinasMap.put(codigo, d);
                    if (!idProf.isBlank()) {
                        profIdPorDisciplina.put(codigo, idProf);
                    }
                }
            }
        }

        // 2. Carrega Cursos
        Map<String, Curso> cursosMap = new LinkedHashMap<>();
        if (secoes.containsKey(SECAO_CURSOS)) {
            for (String linha : secoes.get(SECAO_CURSOS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 3) {
                    String codigo = desescapar(partes[0].trim());
                    String nome = desescapar(partes[1].trim());
                    int totalCreditos = Integer.parseInt(partes[2].trim());
                    Curso c = new Curso(codigo, nome, totalCreditos);

                    if (partes.length >= 4 && !partes[3].isBlank()) {
                        String[] codDisciplinas = partes[3].split(",");
                        for (String codDisc : codDisciplinas) {
                            Disciplina d = disciplinasMap.get(codDisc.trim());
                            if (d != null) {
                                c.adicionarDisciplina(d);
                            }
                        }
                    }
                    cursosMap.put(codigo, c);
                    sistema.cadastrarCurso(c);
                }
            }
        }

        // 3. Carrega Usuários: Secretários, Professores e Alunos
        Map<String, Usuario> usuariosMap = new LinkedHashMap<>();

        // 3.1 Secretários
        if (secoes.containsKey(SECAO_SECRETARIOS)) {
            for (String linha : secoes.get(SECAO_SECRETARIOS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 5) {
                    String id = desescapar(partes[0].trim());
                    String nome = desescapar(partes[1].trim());
                    String email = desescapar(partes[2].trim());
                    String senha = desescapar(partes[3].trim());
                    String cargo = desescapar(partes[4].trim());

                    Secretario s = new Secretario(id, nome, email, senha, cargo);
                    usuariosMap.put(id, s);
                    sistema.cadastrarUsuario(s);
                }
            }
        }

        // 3.2 Professores
        if (secoes.containsKey(SECAO_PROFESSORES)) {
            for (String linha : secoes.get(SECAO_PROFESSORES)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 5) {
                    String id = desescapar(partes[0].trim());
                    String nome = desescapar(partes[1].trim());
                    String email = desescapar(partes[2].trim());
                    String senha = desescapar(partes[3].trim());
                    String siape = desescapar(partes[4].trim());

                    Professor p = new Professor(id, nome, email, senha, siape);
                    if (partes.length >= 6 && !partes[5].isBlank()) {
                        String[] cods = partes[5].split(",");
                        for (String cod : cods) {
                            Disciplina d = disciplinasMap.get(cod.trim());
                            if (d != null) {
                                p.atribuirDisciplina(d);
                            }
                        }
                    }
                    usuariosMap.put(id, p);
                    sistema.cadastrarUsuario(p);
                }
            }
        }

        // Vincula eventuais professores referenciados nas disciplinas que ainda não foram vinculados
        for (Map.Entry<String, String> entry : profIdPorDisciplina.entrySet()) {
            Disciplina d = disciplinasMap.get(entry.getKey());
            Usuario u = usuariosMap.get(entry.getValue());
            if (d != null && u instanceof Professor p && d.getProfessorResponsavel() == null) {
                p.atribuirDisciplina(d);
            }
        }

        // 3.3 Alunos
        if (secoes.containsKey(SECAO_ALUNOS)) {
            for (String linha : secoes.get(SECAO_ALUNOS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 5) {
                    String id = desescapar(partes[0].trim());
                    String nome = desescapar(partes[1].trim());
                    String email = desescapar(partes[2].trim());
                    String senha = desescapar(partes[3].trim());
                    String matricula = desescapar(partes[4].trim());
                    String codCurso = partes.length >= 6 ? desescapar(partes[5].trim()) : "";

                    Curso curso = cursosMap.get(codCurso);
                    Aluno a = new Aluno(id, nome, email, senha, matricula, curso);
                    usuariosMap.put(id, a);
                    sistema.cadastrarUsuario(a);
                }
            }
        }

        // 4. Carrega Currículos
        if (secoes.containsKey(SECAO_CURRICULOS)) {
            for (String linha : secoes.get(SECAO_CURRICULOS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 2) {
                    String semestre = desescapar(partes[0].trim());
                    boolean periodoAberto = Boolean.parseBoolean(partes[1].trim());

                    Curriculo cur = new Curriculo(semestre);
                    cur.setPeriodoMatriculaAberto(periodoAberto);

                    if (partes.length >= 3 && !partes[2].isBlank()) {
                        String[] cods = partes[2].split(",");
                        for (String cod : cods) {
                            Disciplina d = disciplinasMap.get(cod.trim());
                            if (d != null) {
                                cur.adicionarOferta(d);
                            }
                        }
                    }
                    sistema.cadastrarCurriculo(cur);
                }
            }
        }

        // 5. Carrega Matrículas
        Map<String, Matricula> matriculasMap = new LinkedHashMap<>();
        if (secoes.containsKey(SECAO_MATRICULAS)) {
            for (String linha : secoes.get(SECAO_MATRICULAS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 6) {
                    String id = desescapar(partes[0].trim());
                    String idAluno = desescapar(partes[1].trim());
                    String codDisciplina = desescapar(partes[2].trim());
                    TipoMatricula tipo = TipoMatricula.valueOf(partes[3].trim());
                    StatusMatricula status = StatusMatricula.valueOf(partes[4].trim());
                    LocalDateTime dataMatricula = LocalDateTime.parse(partes[5].trim());

                    Aluno aluno = (Aluno) usuariosMap.get(idAluno);
                    Disciplina disciplina = disciplinasMap.get(codDisciplina);

                    Matricula m = new Matricula(id, aluno, disciplina, tipo, status, dataMatricula);
                    matriculasMap.put(id, m);

                    if (aluno != null) {
                        aluno.adicionarMatriculaExistente(m);
                    }
                    if (disciplina != null) {
                        disciplina.adicionarInscricaoExistente(m);
                    }
                }
            }
        }

        // 6. Carrega Cobranças
        if (secoes.containsKey(SECAO_COBRANCAS)) {
            for (String linha : secoes.get(SECAO_COBRANCAS)) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 6) {
                    String id = desescapar(partes[0].trim());
                    String idAluno = desescapar(partes[1].trim());
                    String semestre = desescapar(partes[2].trim());
                    double valorTotal = Double.parseDouble(partes[3].trim());
                    LocalDateTime dataEmissao = LocalDateTime.parse(partes[4].trim());
                    boolean paga = Boolean.parseBoolean(partes[5].trim());

                    Aluno aluno = (Aluno) usuariosMap.get(idAluno);

                    List<Matricula> matriculasCobradas = new ArrayList<>();
                    if (partes.length >= 7 && !partes[6].isBlank()) {
                        String[] idsMats = partes[6].split(",");
                        for (String idMat : idsMats) {
                            Matricula m = matriculasMap.get(idMat.trim());
                            if (m != null) {
                                matriculasCobradas.add(m);
                            }
                        }
                    }

                    Cobranca cobranca = new Cobranca(id, aluno, semestre, valorTotal, dataEmissao, paga, matriculasCobradas);
                    sistema.getSistemaCobranca().adicionarCobranca(cobranca);
                }
            }
        }
    }

    @Override
    public SistemaMatricula carregar() throws IOException {
        return carregar(this.caminhoPadrao);
    }

    @Override
    public SistemaMatricula carregar(String caminhoArquivo) throws IOException {
        SistemaCobranca cobranca = new SistemaCobranca();
        SistemaMatricula sistema = new SistemaMatricula(cobranca);
        carregar(sistema, caminhoArquivo);
        return sistema;
    }

    @Override
    public boolean existeArquivoPersistencia() {
        return existeArquivoPersistencia(this.caminhoPadrao);
    }

    @Override
    public boolean existeArquivoPersistencia(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            return false;
        }
        return Files.exists(Paths.get(caminhoArquivo));
    }

    @Override
    public String getCaminhoPadrao() {
        return this.caminhoPadrao;
    }

    // ==========================================================
    // MÉTODOS AUXILIARES DE PROCESSAMENTO E FORMATAÇÃO
    // ==========================================================

    private Map<String, List<String>> lerSecoes(Path path) throws IOException {
        Map<String, List<String>> secoes = new LinkedHashMap<>();
        String secaoAtual = null;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#") || linha.startsWith("//")) {
                    continue;
                }

                if (linha.startsWith("[") && linha.endsWith("]")) {
                    secaoAtual = linha;
                    secoes.putIfAbsent(secaoAtual, new ArrayList<>());
                } else if (secaoAtual != null) {
                    secoes.get(secaoAtual).add(linha);
                }
            }
        }
        return secoes;
    }

    private Map<String, Disciplina> coletarTodasDisciplinas(SistemaMatricula sistema) {
        Map<String, Disciplina> mapa = new LinkedHashMap<>();

        // Disciplinas em cursos
        for (Curso curso : sistema.getCursos()) {
            for (Disciplina d : curso.getGradeDisciplinas()) {
                mapa.putIfAbsent(d.getCodigo(), d);
            }
        }

        // Disciplinas ofertadas em currículos
        for (Curriculo cur : sistema.getCurriculos()) {
            for (Disciplina d : cur.getDisciplinasOfertadas()) {
                mapa.putIfAbsent(d.getCodigo(), d);
            }
        }

        // Disciplinas de professores
        for (Professor prof : sistema.getProfessores()) {
            for (Disciplina d : prof.getDisciplinasLecionadas()) {
                mapa.putIfAbsent(d.getCodigo(), d);
            }
        }

        // Disciplinas referenciadas em matrículas de alunos
        for (Aluno aluno : sistema.getAlunos()) {
            for (Matricula m : aluno.getMatriculas()) {
                if (m.getDisciplina() != null) {
                    mapa.putIfAbsent(m.getDisciplina().getCodigo(), m.getDisciplina());
                }
            }
        }

        return mapa;
    }

    private Map<String, Matricula> coletarTodasMatriculas(SistemaMatricula sistema, Map<String, Disciplina> disciplinas) {
        Map<String, Matricula> mapa = new LinkedHashMap<>();

        // Matrículas nos alunos
        for (Aluno a : sistema.getAlunos()) {
            for (Matricula m : a.getMatriculas()) {
                mapa.putIfAbsent(m.getId(), m);
            }
        }

        // Inscrições nas disciplinas
        for (Disciplina d : disciplinas.values()) {
            for (Matricula m : d.getInscricoes()) {
                mapa.putIfAbsent(m.getId(), m);
            }
        }

        // Matrículas nas cobranças emitidas
        for (Cobranca cob : sistema.getSistemaCobranca().getCobrancasEmitidas()) {
            for (Matricula m : cob.getMatriculasCobradas()) {
                mapa.putIfAbsent(m.getId(), m);
            }
        }

        return mapa;
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace(";", "\\;").replace("\n", "\\n").replace("\r", "");
    }

    private String desescapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\n", "\n").replace("\\;", ";").replace("\\\\", "\\");
    }
}

