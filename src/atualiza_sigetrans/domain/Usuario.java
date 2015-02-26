package atualiza_sigetrans.domain;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.model.SelectItem;
import javax.persistence.*;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 *
 * @author Leonardo Merlin <leonardo.merlin at unioeste.br>
 */
@Entity
@Table(name = "usuario", schema = "usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false, unique = true, updatable = false)
    private String login;
    @Column(nullable = false)
    private String senha;
    @Column(nullable = false)
    private String nome;  //nome completo
    private String cpf;   //Informar somente numeros
    private String localDeTrabalho;  //nome completo
    private String telefoneContarto;  //nome completo
    private boolean ativo;
    //
    @OneToOne
    @ForeignKey(name = "FK_USUARIO_CRIADOR")
    private Usuario criador;
    //
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriacao;
    //
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "usuario_autorizacao",
    schema = "usuarios",
    joinColumns = {
        @JoinColumn(name = "usuario_id")},
    inverseJoinColumns = {
        @JoinColumn(name = "autorizacao_id")})
    @ForeignKey(name = "FK_USUARIO", inverseName = "FK_AUTORIZACAO")
    private List<Autorizacao> autorizacoes;
    //
    @Transient
    private Autorizacao autorizacao;
    @Transient
    private Grupo grupo;

    public Usuario() {
        this.nome = "Insira o Nome Completo";
        this.login = "login";
        this.senha = "senha";
        this.autorizacoes = new ArrayList<>();
    }

    public Usuario(String nome) {
        this.nome = nome;
        this.login = nome;
        this.senha = nome;
        this.autorizacoes = new ArrayList<>();
    }

    public Usuario(String nome, String senha) {
        this.nome = nome;
        this.login = nome;
        this.senha = senha;
        this.autorizacoes = new ArrayList<>();
    }

    public Usuario(String login, String senha, String nome, boolean ativo, Usuario criador, Date dataCriacao) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.ativo = ativo;
        this.criador = criador;
        this.dataCriacao = dataCriacao;
    }

    /**
     *
     * @param nome
     * @param senha
     * @param autorizacoes
     */
    public Usuario(String nome, String senha, List<Autorizacao> autorizacoes) {
        this.nome = nome;
        this.login = nome;
        this.senha = senha;
        this.autorizacoes = autorizacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Usuario getCriador() {
        return criador;
    }

    public void setCriador(Usuario criador) {
        this.criador = criador;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getLocalDeTrabalho() {
        return localDeTrabalho;
    }

    public void setLocalDeTrabalho(String localDeTrabalho) {
        this.localDeTrabalho = localDeTrabalho;
    }

    /**
     * @return the autorizacoes
     */
    public List<Autorizacao> getAutorizacoes() {
        if (autorizacoes == null) {
            autorizacoes = new ArrayList<>();
        }
        return autorizacoes;
    }

    /**
     * @param autorizacoes the autorizacoes to set
     */
    public void setAutorizacoes(List<Autorizacao> autorizacoes) {
        this.autorizacoes = autorizacoes;
    }

    public Autorizacao getAutorizacao() {
        if (autorizacao == null) {
            autorizacao = autorizacoes.iterator().next();
        }
        return autorizacao;
    }

    public void setAutorizacao(Autorizacao autorizacao) {
        this.autorizacao = autorizacao;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean desativado() {
        return !ativo;
    }

    public String getTelefoneContarto() {
        return telefoneContarto;
    }

    public void setTelefoneContarto(String telefoneContarto) {
        this.telefoneContarto = telefoneContarto;
    }

    public List<String> getAutorizacoesString() {
        List<String> aut = new ArrayList<>();
        for (int i = 0; i < autorizacoes.size(); i++) {
            Autorizacao element = autorizacoes.get(i);
            //Remoção do início "ROLE_" para mostrar ao usuário
            Pattern padrao = Pattern.compile("ROLE_");
            Matcher matcher = padrao.matcher(element.getRegra());
            if (matcher.find()) {
                aut.add(element.getRegra().replaceAll(padrao.pattern(), ""));
            }
        }
        return aut;
    }
}
