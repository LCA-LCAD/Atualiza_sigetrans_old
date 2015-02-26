package atualiza_sigetrans.domain;

import atualiza_sigetrans.domain.Envolvido;
import atualiza_sigetrans.domain.Endereco;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

/**
 *
 * @author Henrique
 */
@Entity
@Table(name = "ocorrencia")
public class Ocorrencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Index(name = "bat_id")
    @Column(columnDefinition = "varchar(20)", name = "bat_id", unique = true, nullable = true)
    private String numeroBAT;
    @Index(name = "data")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataAcidente;
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date horarioAcidente;
    @Embedded
    private Endereco endereco;
    @Column(columnDefinition = "Text")
    private String observacao;
    //
    @OneToMany(mappedBy = "ocorrencia")
    @ForeignKey(name = "FK_OCORRENCIA", inverseName = "FK_ENVOLVIDO")
    private List<Envolvido> envolvidos;
    //
    private String sinalizacao;
    private String tipoAcidente;
    private String superficie;
    private String conservacao;
    private String pavimentacao;
    private String perfilPista;
    private String severidade;
    private String provavelCausa;
    private String clima;
    private Integer quantidadeVeiculos;
    private Boolean zonaRural;
    //
    @Embedded
    private ControleUsuario controleUsuario;

    public Ocorrencia() {
        endereco = new Endereco();
        dataAcidente = new Date();
        envolvidos = new ArrayList<>();
        controleUsuario = new ControleUsuario();
        tipoAcidente = "Não informado";
        severidade = "Não informado";
        sinalizacao = "Não informado";
        superficie = "Não informado";
        pavimentacao = "Não informado";
        provavelCausa = "Não apurado";
        conservacao = "Não informado";
        perfilPista = "Não informado";
        clima = "Não informado";
        quantidadeVeiculos = 0; //so para não retornar null no formulario Ocorrencia
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataAcidente() {
        return dataAcidente;
    }

    public void setDataAcidente(Date dataAcidente) {
        this.dataAcidente = dataAcidente;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Date getHorarioAcidente() {
        return horarioAcidente;
    }

    public void setHorarioAcidente(Date horarioAcidente) {
        this.horarioAcidente = horarioAcidente;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public ControleUsuario getControleUsuario() {
        return controleUsuario;
    }

    public void setControleUsuario(ControleUsuario dataUserControl) {
        this.controleUsuario = dataUserControl;
    }

    public String getNumeroBAT() {
        return numeroBAT;
    }

    public void setNumeroBAT(String numeroBAT) {
        this.numeroBAT = numeroBAT;
    }

    public List<Envolvido> getEnvolvidos() {
        return envolvidos;
    }

    public void setEnvolvidos(List<Envolvido> envolvidos) {
        this.envolvidos = envolvidos;
    }

    public Boolean getZonaRural() {
        return zonaRural;
    }

    public void setZonaRural(Boolean zonaRural) {
        this.zonaRural = zonaRural;
    }

    public String getTipoAcidente() {
        return tipoAcidente;
    }

    public void setTipoAcidente(String tipoAcidente) {
        this.tipoAcidente = tipoAcidente;
    }

    public String getSuperficie() {
        return superficie;
    }

    public void setSuperficie(String superficie) {
        this.superficie = superficie;
    }

    public String getConservacao() {
        return conservacao;
    }

    public void setConservacao(String conservacao) {
        this.conservacao = conservacao;
    }

    public String getPavimentacao() {
        return pavimentacao;
    }

    public void setPavimentacao(String pavimentacao) {
        this.pavimentacao = pavimentacao;
    }

    public String getPerfilPista() {
        return perfilPista;
    }

    public void setPerfilPista(String perfilPista) {
        this.perfilPista = perfilPista;
    }

    public String getSeveridade() {
        return severidade;
    }

    public void setSeveridade(String severidade) {
        this.severidade = severidade;
    }

    public String getProvavelCausa() {
        return provavelCausa;
    }

    public void setProvavelCausa(String provavelCausa) {
        this.provavelCausa = provavelCausa;
    }

    public String getClima() {
        return clima;
    }

    public void setClima(String clima) {
        this.clima = clima;
    }

    public String getSinalizacao() {
        return sinalizacao;
    }

    public void setSinalizacao(String sinalizacao) {
        this.sinalizacao = sinalizacao;
    }

    public Integer getQuantidadeVeiculos() {
        return quantidadeVeiculos;
    }

    public void setQuantidadeVeiculos(Integer quantidadeVeiculos) {
        this.quantidadeVeiculos = quantidadeVeiculos;
    }
    
    public boolean hasBAT(){
        return numeroBAT != null && !numeroBAT.isEmpty();
    }
    
    public boolean notHasBAT(){
        return numeroBAT == null || numeroBAT.isEmpty() ;
    }
}
