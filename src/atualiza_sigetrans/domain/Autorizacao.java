package atualiza_sigetrans.domain;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.*;

/**
 *
 * @author Leonardo Merlin <leonardo.merlin at unioeste.br>
 */
@Entity
@Table(name = "autorizacao", schema = "usuarios")
public class Autorizacao implements Serializable {

    public static Autorizacao ROLE_SUPERVISOR = new Autorizacao("ROLE_SUPERVISOR");
    public static Autorizacao ROLE_BOMBEIROS = new Autorizacao("ROLE_BOMBEIROS");
    public static Autorizacao ROLE_CETTRANS = new Autorizacao("ROLE_CETTRANS");
    public static Autorizacao ROLE_HOSPITAL = new Autorizacao("ROLE_HOSPITAIS");
    public static Autorizacao ROLE_UBS = new Autorizacao("ROLE_UBS");
    //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id_autorizacao")
    private Long id;
    @Column(unique = true, nullable = false, updatable = false)
    private String regra; // role
    @ManyToMany(mappedBy = "autorizacoes")
    private List<Usuario> usuarios;

    public Autorizacao() {
        this.regra = "ROLE_NONE";
    }

    public Autorizacao(String regra) {
        this.regra = regra;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegra() {
        return regra;
    }

    public String regraToView() {
        Pattern padrao = Pattern.compile("ROLE_");
        Matcher matcher = padrao.matcher(regra);
        if (matcher.find()) {
            return regra.replaceAll(padrao.pattern(), "");
        } else {
            return regra;
        }
    }

    public void setRegra(String regra) {
        this.regra = regra;
    }

    /**
     * @return the usuarios
     */
    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    /**
     * @param usuarios the usuarios to set
     */
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
