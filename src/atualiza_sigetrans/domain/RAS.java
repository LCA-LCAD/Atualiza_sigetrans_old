package atualiza_sigetrans.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Henrique
 */
@Entity
@Table(schema = "formularios")
public class RAS implements Serializable {
    //

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long rgo;
    @OneToOne
    private Ocorrencia ocorrencia;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataAcidente;
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date horarioAcidente;
    @Embedded
    private Endereco endereco;
    private String tipoAcidente;
    @OneToMany(mappedBy = "ras")
    private List<VitimaRAS> listVitima;
    @Embedded
    private ControleUsuario controleUsuario;

    public RAS() {
        endereco = new Endereco();
        dataAcidente = new Date();
        listVitima = new ArrayList<>();

        tipoAcidente = "Não informado";
        controleUsuario = new ControleUsuario();
    }

    public ControleUsuario getControleUsuario() {
        return controleUsuario;
    }

    public void setControleUsuario(ControleUsuario controleUsuario) {
        this.controleUsuario = controleUsuario;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRgo() {
        return rgo;
    }

    public void setRgo(Long rgo) {
        this.rgo = rgo;
    }

    public List<VitimaRAS> getListVitima() {
        return listVitima;
    }

    public void setListVitima(List<VitimaRAS> listVitima) {
        this.listVitima = listVitima;
    }

    public String getTipoAcidente() {
        return tipoAcidente;
    }

    public void setTipoAcidente(String tipoAcidente) {
        this.tipoAcidente = tipoAcidente;
    }

    public Date getDataAcidente() {
        return dataAcidente;
    }

    public void setDataAcidente(Date dataAcidente) {
        this.dataAcidente = dataAcidente;
    }

    public Date getHorarioAcidente() {
        return horarioAcidente;
    }

    public void setHorarioAcidente(Date horarioAcidente) {
        this.horarioAcidente = horarioAcidente;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }
}
