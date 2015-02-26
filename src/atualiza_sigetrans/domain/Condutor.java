package atualiza_sigetrans.domain;

import atualiza_sigetrans.enumerate.EnumEstadoCondutor;
import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Henrique
 */
@Entity
@Table(name = "Condutor")
public class Condutor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // DADOS DO CONDUTOR
    private String bafometro;
    private String dosagem;
    private boolean solicitacaoExame;
    private boolean sintomasEmbriaguez;
    //Enum...
    @Enumerated(EnumType.STRING)
    private EnumEstadoCondutor estadoCondutor; //Sem ferimentos, com ferimentos, óbito no local, óbito posterior
    //Condicao de segurança, vindo do envolvido
    @Embedded
    private Habilitacao habilitacao;
    //
    @OneToOne(cascade= CascadeType.ALL)
    private Veiculo veiculo;

    public Condutor() {
        habilitacao = new Habilitacao();
        estadoCondutor = EnumEstadoCondutor.NAO_INFORMADO;
        veiculo = new Veiculo();
    }

    public String getBafometro() {
        return bafometro;
    }

    public void setBafometro(String bafometro) {
        this.bafometro = bafometro;

    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;

    }

    public EnumEstadoCondutor getEstadoCondutor() {
        return estadoCondutor;
    }

    public void setEstadoCondutor(EnumEstadoCondutor estadoCondutor) {
        this.estadoCondutor = estadoCondutor;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;

    }

    public boolean isSintomasEmbriaguez() {
        return sintomasEmbriaguez;
    }

    public void setSintomasEmbriaguez(boolean sintomasEmbriaguez) {
        this.sintomasEmbriaguez = sintomasEmbriaguez;

    }

    public boolean isSolicitacaoExame() {
        return solicitacaoExame;
    }

    public void setSolicitacaoExame(boolean solicitacaoExame) {
        this.solicitacaoExame = solicitacaoExame;

    }

    public Habilitacao getHabilitacao() {
        return habilitacao;
    }

    public void setHabilitacao(Habilitacao habilitacao) {
        this.habilitacao = habilitacao;

    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;

    }
}
