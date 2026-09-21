package br.edu.pucminas.matricula.service;

import br.edu.pucminas.matricula.enums.TipoMatricula;
import br.edu.pucminas.matricula.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fachada principal e controlador do Sistema de Matrículas Universitário.
 * Orquestra as regras de negócio de inscrições, cancelamentos, encerramento de período e integração com cobrança.
 */
public class SistemaMatricula {
    private final List<Usuario> usuarios;
    private final List<Curso> cursos;
    private final List<Curriculo> curriculos;
    private final SistemaCobranca sistemaCobranca;

    public SistemaMatricula(SistemaCobranca sistemaCobranca) {
        this.usuarios = new ArrayList<>();
        this.cursos = new ArrayList<>();
        this.curriculos = new ArrayList<>();
        this.sistemaCobranca = sistemaCobranca != null ? sistemaCobranca : new SistemaCobranca();
    }

    /**
     * Stub para autenticação de qualquer tipo de usuário no sistema com e-mail/id e senha.
     *
     * @param identificador E-mail ou ID do usuário
     * @param senha Senha do usuário
     * @return Usuário autenticado ou null se credenciais forem inválidas
     */
    public Usuario autenticarUsuario(String identificador, String senha) {
        if (identificador == null || senha == null) {
            return null;
        }
        for (Usuario u : this.usuarios) {
            if ((identificador.equalsIgnoreCase(u.getEmail()) || identificador.equals(u.getId())) && u.autenticar(senha)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Stub para solicitação de matrícula pelo aluno.
     * Valida o período de matrícula aberto, aplica as regras (até 4 obrigatórias, até 2 optativas, máx 60 vagas)
     * e aciona o sistema de cobrança.
     *
     * @param aluno Aluno solicitante
     * @param disciplina Disciplina desejada
     * @param tipo Tipo de matrícula (OBRIGATORIA ou OPTATIVA)
     * @param curriculo Currículo do semestre vigente
     * @return true se a matrícula foi efetuada e notificada com sucesso
     */
    public boolean solicitarMatricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo, Curriculo curriculo) {
        if (curriculo == null || !curriculo.isPeriodoMatriculaAberto()) {
            System.out.println("[SISTEMA] Período de matrículas fechado ou inexistente.");
            return false;
        }

        if (!curriculo.getDisciplinasOfertadas().contains(disciplina)) {
            System.out.println("[SISTEMA] Disciplina não ofertada no currículo do semestre.");
            return false;
        }

        Matricula novaMatricula = aluno.matricular(disciplina, tipo);
        if (novaMatricula != null) {
            // Notifica o sistema de cobranças para faturamento do semestre
            this.sistemaCobranca.gerarCobranca(aluno, curriculo.getSemestre(), aluno.getMatriculas());
            return true;
        }

        return false;
    }

    /**
     * Stub para cancelamento de matrícula pelo aluno durante o período aberto.
     *
     * @param aluno Aluno solicitante
     * @param disciplina Disciplina a ser cancelada
     * @param curriculo Currículo do semestre vigente
     * @return true se cancelada com sucesso
     */
    public boolean solicitarCancelamento(Aluno aluno, Disciplina disciplina, Curriculo curriculo) {
        if (curriculo == null || !curriculo.isPeriodoMatriculaAberto()) {
            System.out.println("[SISTEMA] Período de matrículas fechado para alterações.");
            return false;
        }
        return aluno.cancelarMatricula(disciplina);
    }

    /**
     * Stub para finalizar o período de matrículas.
     * Fecha as inscrições e dispara a verificação automática de quórum (mínimo 3 alunos).
     *
     * @param curriculo Currículo a ser encerrado
     */
    public void finalizarPeriodoMatricula(Curriculo curriculo) {
        if (curriculo != null) {
            curriculo.fecharPeriodo();
            System.out.println("[SISTEMA] Período de matrículas encerrado para o semestre " + curriculo.getSemestre());
        }
    }

    public void cadastrarUsuario(Usuario usuario) {
        if (usuario != null && !this.usuarios.contains(usuario)) {
            this.usuarios.add(usuario);
        }
    }

    public void cadastrarCurso(Curso curso) {
        if (curso != null && !this.cursos.contains(curso)) {
            this.cursos.add(curso);
        }
    }

    public void cadastrarCurriculo(Curriculo curriculo) {
        if (curriculo != null && !this.curriculos.contains(curriculo)) {
            this.curriculos.add(curriculo);
        }
    }

    public Curriculo buscarCurriculo(String semestre) {
        if (semestre == null) return null;
        for (Curriculo c : this.curriculos) {
            if (c.getSemestre().equalsIgnoreCase(semestre.trim())) {
                return c;
            }
        }
        return null;
    }

    public Curriculo getCurriculoVigente() {
        // Retorna o currículo aberto ou o último cadastrado
        for (Curriculo c : this.curriculos) {
            if (c.isPeriodoMatriculaAberto()) {
                return c;
            }
        }
        return this.curriculos.isEmpty() ? null : this.curriculos.get(this.curriculos.size() - 1);
    }

    public Curso buscarCurso(String codigo) {
        if (codigo == null) return null;
        for (Curso c : this.cursos) {
            if (c.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return c;
            }
        }
        return null;
    }

    public Disciplina buscarDisciplina(String codigo) {
        if (codigo == null) return null;
        for (Curso c : this.cursos) {
            for (Disciplina d : c.getGradeDisciplinas()) {
                if (d.getCodigo().equalsIgnoreCase(codigo.trim())) {
                    return d;
                }
            }
        }
        for (Curriculo curr : this.curriculos) {
            for (Disciplina d : curr.getDisciplinasOfertadas()) {
                if (d.getCodigo().equalsIgnoreCase(codigo.trim())) {
                    return d;
                }
            }
        }
        return null;
    }

    public List<Aluno> getAlunos() {
        List<Aluno> alunos = new ArrayList<>();
        for (Usuario u : this.usuarios) {
            if (u instanceof Aluno a) {
                alunos.add(a);
            }
        }
        return Collections.unmodifiableList(alunos);
    }

    public List<Professor> getProfessores() {
        List<Professor> professores = new ArrayList<>();
        for (Usuario u : this.usuarios) {
            if (u instanceof Professor p) {
                professores.add(p);
            }
        }
        return Collections.unmodifiableList(professores);
    }

    public List<Secretario> getSecretarios() {
        List<Secretario> secretarios = new ArrayList<>();
        for (Usuario u : this.usuarios) {
            if (u instanceof Secretario s) {
                secretarios.add(s);
            }
        }
        return Collections.unmodifiableList(secretarios);
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public List<Curso> getCursos() {
        return Collections.unmodifiableList(cursos);
    }

    public List<Curriculo> getCurriculos() {
        return Collections.unmodifiableList(curriculos);
    }

    public SistemaCobranca getSistemaCobranca() {
        return sistemaCobranca;
    }
}
