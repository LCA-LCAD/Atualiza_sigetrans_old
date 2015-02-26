package atualiza_sigetrans.domain;

import atualiza_sigetrans.domain.Usuario;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;

/**
 *
 * @author Henrique
 */
@Embeddable
public class ControleUsuario implements Serializable {

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dataCriacao;
    private String usuario_login;

    public ControleUsuario(Date dataCriacao, String usuario_login) {
        this.dataCriacao = dataCriacao;
        this.usuario_login = usuario_login;
    }

    public String getUsuario_login() {
        return usuario_login;
    }

    public void setUsuario_login(String usuario_login) {
        this.usuario_login = usuario_login;
    }

    public ControleUsuario() {
        dataCriacao = new Date();
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}