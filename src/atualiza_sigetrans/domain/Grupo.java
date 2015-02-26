package atualiza_sigetrans.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 *
 * @author Leonardo Merlin <leonardo.merlin at unioeste.br>
 */
@Entity
@Table(name = "grupo", schema = "usuarios")
public class Grupo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    @Column(unique = true)
    private String nome;
    //
    @ManyToMany(fetch= FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(schema = "usuarios")
    @ForeignKey(name = "FK_GRUPOS", inverseName = "FK_USUARIOS")
    private Set<Usuario> usuarios;
    @ManyToMany(cascade= CascadeType.ALL)
    @JoinTable(schema = "usuarios")
    @ForeignKey(name = "FK_GRUPOS", inverseName = "FK_AUTORIZACOES")
    private Set<Autorizacao> autorizacoes;

    public Grupo() {
        this.nome = "Nome do Grupo";
        this.usuarios = new HashSet<>();
        this.autorizacoes = new HashSet<>();
    }

    public Grupo(String nome) {
        this.nome = nome;
        this.usuarios = new HashSet<>();
        this.autorizacoes = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Autorizacao> getAutorizacoes() {
        return autorizacoes;
    }

    public void setAutorizacoes(Set<Autorizacao> autorizacoes) {
        this.autorizacoes = autorizacoes;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
