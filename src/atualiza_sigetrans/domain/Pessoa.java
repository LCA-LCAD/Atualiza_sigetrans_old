package atualiza_sigetrans.domain;

import atualiza_sigetrans.enumerate.EnumSexo;
import atualiza_sigetrans.enumerate.EnumUnidadeFederativa;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Henrique
 */
@Entity
@Table(name = "Pessoa")
public class Pessoa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String nome;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String nomeMae;
    private String rg;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String rua;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String complemento;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String numero;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String bairro;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String cidade;
    private String telefone;
    private String cartaoSUS;
    @Column(columnDefinition = "varchar(15) DEFAULT ''")
    private String cpf;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataNascimento;
    @Enumerated(EnumType.STRING)
    private EnumSexo sexo;
    @Enumerated(EnumType.STRING)
    private EnumUnidadeFederativa estado;
    //
    @Embedded
    private ControleUsuario controleUsuario;

    public Pessoa() {
        nome = "";
        rua = "";
        controleUsuario = new ControleUsuario();
    }

    public Pessoa(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeMae() {
        return nomeMae;
    }

    public void setNomeMae(String nomeMae) {
        this.nomeMae = nomeMae;
    }

    public EnumSexo getSexo() {
        return sexo;
    }

    public void setSexo(EnumSexo sexo) {
        this.sexo = sexo;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public EnumUnidadeFederativa getEstado() {
        return estado;
    }

    public void setEstado(EnumUnidadeFederativa estado) {
        this.estado = estado;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getCartaoSUS() {
        return cartaoSUS;
    }

    public void setCartaoSUS(String cartaoSUS) {
        this.cartaoSUS = cartaoSUS;
    }

    public ControleUsuario getControleUsuario() {
        return controleUsuario;
    }

    public void setControleUsuario(ControleUsuario controleUsuario) {
        this.controleUsuario = controleUsuario;
    }
}
