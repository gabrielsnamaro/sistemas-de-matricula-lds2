package br.edu.pucminas.matricula.model;

import java.util.Objects;

/**
 * Classe base abstrata para todos os usuários do sistema universitário.
 * Atende aos requisitos de autenticação unificada e controle de credenciais.
 */
public abstract class Usuario {
    private String id;
    private String nome;
    private String email;
    private String senha;

    public Usuario(String id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    /**
     * Valida a senha fornecida para autenticação no sistema.
     *
     * @param senhaInformada Senha em texto para validação
     * @return true se a senha coincidir, false caso contrário
     */
    public boolean autenticar(String senhaInformada) {
        // Stub de validação de login
        if (this.senha == null || senhaInformada == null) {
            return false;
        }
        return this.senha.equals(senhaInformada);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
