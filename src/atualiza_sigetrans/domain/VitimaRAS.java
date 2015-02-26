package atualiza_sigetrans.domain;

import atualiza_sigetrans.domain.Envolvido;
import atualiza_sigetrans.enumerate.EnumRespiracaoMinuto;
import atualiza_sigetrans.enumerate.EnumPAMaxima;
import atualiza_sigetrans.enumerate.EnumRespostaMotora;
import atualiza_sigetrans.enumerate.EnumEscalaComa;
import atualiza_sigetrans.enumerate.EnumSexo;
import atualiza_sigetrans.enumerate.EnumRespostaVerbal;
import atualiza_sigetrans.enumerate.EnumCondicaoSeguranca;
import atualiza_sigetrans.enumerate.EnumAberturaOcular;
import atualiza_sigetrans.enumerate.EnumPosicaoNoVeiculo;
import atualiza_sigetrans.enumerate.*;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author Henrique
 */
@Entity
@Table(schema = "formularios")
public class VitimaRAS implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long numeroVitima;
    @ManyToOne
    @ForeignKey(name = "FK_RAS", inverseName = "FK_VITIMA")
    private RAS ras;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String nomeVitima;
    @Column(columnDefinition = "varchar(255) DEFAULT 'Não informado'")
    private String enderecoVitima;
    private Integer idade;
    @Enumerated(EnumType.STRING)
    private EnumSexo sexo;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataNascimento;
    private String telefone;
    @Enumerated(EnumType.STRING)
    private EnumCondicaoSeguranca tipoCondicaoDeSeguranca;
    @Enumerated(EnumType.STRING)
    private EnumAberturaOcular aberturaOcular;
    @Enumerated(EnumType.STRING)
    private EnumRespostaMotora respostaMotora;
    @Enumerated(EnumType.STRING)
    private EnumRespostaVerbal respostaVerbal;
    private Integer escalaGlasgow;
    @Enumerated(EnumType.STRING)
    private EnumEscalaComa escalaComa;
    @Enumerated(EnumType.STRING)
    private EnumRespiracaoMinuto respiracaoMinuto;
    @Enumerated(EnumType.STRING)
    private EnumPAMaxima paMaxima;
    @Enumerated(EnumType.STRING)
    private EnumPosicaoNoVeiculo tipoPosicaoVeiculo;
    private Boolean atendimentoMedicoAcompanhamentoHospital;
    private Boolean atendimentoMedicoCompareceu;
    private Boolean atendimentoMedicoIntervencaoMedica;
    private Boolean atendimentoMedicoSolicitado;
    private String escalaComaFinal;
    private String escalaTraumaFinal;
    private String frequenciaRespFinal;
    private String observacao;
    private String paFinal;
    private String pulsoFinal;
    private String saO2Final;
    private String socorristaReponsavel;
    private String superficeQueimada;
    private String temperaturaFinal;
    private String tipoQueimadura;
    private Boolean viasAreas;
    private String tipoSituacao;
    private String tipoVeiculo;
    private String hospital;
    private String destinoDaVitima;
    private String outroDestino;
    @Column(columnDefinition = "Text")
    private String sinaisClinicos;
    @Column(columnDefinition = "Text")
    private String procedimentos;
    @Embedded
    private Lesao lesoesAparentes;
    //
    @OneToOne(mappedBy = "vitimaRAS")
    private Envolvido envolvido;
    //
    @Embedded
    private ControleUsuario controleUsuario;

    public VitimaRAS() {
        tipoPosicaoVeiculo = EnumPosicaoNoVeiculo.NAO_INFORMADO;
        controleUsuario = new ControleUsuario();
        tipoSituacao = "Não informado";
        tipoVeiculo = "Não informado";
        destinoDaVitima = "Não informado";
        hospital = "Não informado";
        lesoesAparentes = new Lesao();
    }

    public Boolean getAtendimentoMedicoAcompanhamentoHospital() {
        return atendimentoMedicoAcompanhamentoHospital;
    }

    public void setAtendimentoMedicoAcompanhamentoHospital(Boolean atendimentoMedicoAcompanhamentoHospital) {
        this.atendimentoMedicoAcompanhamentoHospital = atendimentoMedicoAcompanhamentoHospital;
    }

    public Boolean getAtendimentoMedicoCompareceu() {
        return atendimentoMedicoCompareceu;
    }

    public void setAtendimentoMedicoCompareceu(Boolean atendimentoMedicoCompareceu) {
        this.atendimentoMedicoCompareceu = atendimentoMedicoCompareceu;
    }

    public Boolean getAtendimentoMedicoIntervencaoMedica() {
        return atendimentoMedicoIntervencaoMedica;
    }

    public void setAtendimentoMedicoIntervencaoMedica(Boolean atendimentoMedicoIntervencaoMedica) {
        this.atendimentoMedicoIntervencaoMedica = atendimentoMedicoIntervencaoMedica;
    }

    public Boolean getAtendimentoMedicoSolicitado() {
        return atendimentoMedicoSolicitado;
    }

    public void setAtendimentoMedicoSolicitado(Boolean atendimentoMedicoSolicitado) {
        this.atendimentoMedicoSolicitado = atendimentoMedicoSolicitado;
    }

    public String getEscalaComaFinal() {
        return escalaComaFinal;
    }

    public void setEscalaComaFinal(String escalaComaFinal) {
        this.escalaComaFinal = escalaComaFinal;
    }

    public String getEscalaTraumaFinal() {
        return escalaTraumaFinal;
    }

    public void setEscalaTraumaFinal(String escalaTraumaFinal) {
        this.escalaTraumaFinal = escalaTraumaFinal;
    }

    public String getFrequenciaRespFinal() {
        return frequenciaRespFinal;
    }

    public void setFrequenciaRespFinal(String frequenciaRespFinal) {
        this.frequenciaRespFinal = frequenciaRespFinal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getPaFinal() {
        return paFinal;
    }

    public void setPaFinal(String paFinal) {
        this.paFinal = paFinal;
    }

    public String getPulsoFinal() {
        return pulsoFinal;
    }

    public void setPulsoFinal(String pulsoFinal) {
        this.pulsoFinal = pulsoFinal;
    }

    public EnumEscalaComa getEscalaComa() {
        return escalaComa;
    }

    public void setEscalaComa(EnumEscalaComa escalaComa) {
        this.escalaComa = escalaComa;
    }

    public EnumPAMaxima getPaMaxima() {
        return paMaxima;
    }

    public void setPaMaxima(EnumPAMaxima paMaxima) {
        this.paMaxima = paMaxima;
    }

    public EnumRespiracaoMinuto getRespiracaoMinuto() {
        return respiracaoMinuto;
    }

    public void setRespiracaoMinuto(EnumRespiracaoMinuto respiracaoMinuto) {
        this.respiracaoMinuto = respiracaoMinuto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumeroVitima() {
        return numeroVitima;
    }

    public void setNumeroVitima(Long numeroVitima) {
        this.numeroVitima = numeroVitima;
    }

    public RAS getRas() {
        return ras;
    }

    public void setRas(RAS ras) {
        this.ras = ras;
    }

    public EnumAberturaOcular getAberturaOcular() {
        return aberturaOcular;
    }

    public void setAberturaOcular(EnumAberturaOcular aberturaOcular) {
        this.aberturaOcular = aberturaOcular;
    }

    public EnumRespostaMotora getRespostaMotora() {
        return respostaMotora;
    }

    public void setRespostaMotora(EnumRespostaMotora respostaMotora) {
        this.respostaMotora = respostaMotora;
    }

    public EnumRespostaVerbal getRespostaVerbal() {
        return respostaVerbal;
    }

    public void setRespostaVerbal(EnumRespostaVerbal respostaVerbal) {
        this.respostaVerbal = respostaVerbal;
    }

    public String getSaO2Final() {
        return saO2Final;
    }

    public void setSaO2Final(String saO2Final) {
        this.saO2Final = saO2Final;
    }

    public String getSinaisClinicos() {
        return sinaisClinicos;
    }

    public void setSinaisClinicos(String sinaisClinicos) {
        this.sinaisClinicos = sinaisClinicos;
    }

    public String getProcedimentos() {
        return procedimentos;
    }

    public void setProcedimentos(String procedimentos) {
        this.procedimentos = procedimentos;
    }

    public Lesao getLesoesAparentes() {
        return lesoesAparentes;
    }

    public void setLesoesAparentes(Lesao lesoesAparentes) {
        this.lesoesAparentes = lesoesAparentes;
    }

    public String getSocorristaReponsavel() {
        return socorristaReponsavel;
    }

    public void setSocorristaReponsavel(String socorristaReponsavel) {
        this.socorristaReponsavel = socorristaReponsavel;
    }

    public String getSuperficeQueimada() {
        return superficeQueimada;
    }

    public void setSuperficeQueimada(String superficeQueimada) {
        this.superficeQueimada = superficeQueimada;
    }

    public String getTemperaturaFinal() {
        return temperaturaFinal;
    }

    public void setTemperaturaFinal(String temperaturaFinal) {
        this.temperaturaFinal = temperaturaFinal;
    }

    public EnumCondicaoSeguranca getTipoCondicaoDeSeguranca() {
        return tipoCondicaoDeSeguranca;
    }

    public void setTipoCondicaoDeSeguranca(EnumCondicaoSeguranca tipoCondicaoDeSeguranca) {
        this.tipoCondicaoDeSeguranca = tipoCondicaoDeSeguranca;
    }

    public EnumPosicaoNoVeiculo getTipoPosicaoVeiculo() {
        return tipoPosicaoVeiculo;
    }

    public void setTipoPosicaoVeiculo(EnumPosicaoNoVeiculo tipoPosicaoVeiculo) {
        this.tipoPosicaoVeiculo = tipoPosicaoVeiculo;
    }

    public String getTipoQueimadura() {
        return tipoQueimadura;
    }

    public void setTipoQueimadura(String tipoQueimadura) {
        this.tipoQueimadura = tipoQueimadura;
    }

    public String getTipoSituacao() {
        return tipoSituacao;
    }

    public void setTipoSituacao(String tipoSituacao) {
        this.tipoSituacao = tipoSituacao;
    }

    public String getTipoVeiculo() {
        return tipoVeiculo;
    }

    public void setTipoVeiculo(String tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getDestinoDaVitima() {
        return destinoDaVitima;
    }

    public void setDestinoDaVitima(String destinoDaVitima) {
        this.destinoDaVitima = destinoDaVitima;
    }

    public Boolean getViasAreas() {
        return viasAreas;
    }

    public void setViasAreas(Boolean viasAreas) {
        this.viasAreas = viasAreas;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEnderecoVitima() {
        return enderecoVitima;
    }

    public void setEnderecoVitima(String enderecoVitima) {
        this.enderecoVitima = enderecoVitima;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public String getNomeVitima() {
        return nomeVitima;
    }

    public void setNomeVitima(String nomeVitima) {
        this.nomeVitima = nomeVitima;
    }

    public EnumSexo getSexo() {
        return sexo;
    }

    public void setSexo(EnumSexo sexo) {
        this.sexo = sexo;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Envolvido getEnvolvido() {
        return envolvido;
    }

    public void setEnvolvido(Envolvido envolvido) {
        this.envolvido = envolvido;
    }

    public ControleUsuario getControleUsuario() {
        return controleUsuario;
    }

    public void setControleUsuario(ControleUsuario controleUsuario) {
        this.controleUsuario = controleUsuario;
    }

    public Integer getEscalaGlasgow() {
        return escalaGlasgow;
    }

    public void setEscalaGlasgow(Integer escalaGlasgow) {
        this.escalaGlasgow = escalaGlasgow;
    }

    public String getOutroDestino() {
        return outroDestino;
    }

    public void setOutroDestino(String outroDestino) {
        this.outroDestino = outroDestino;
    }
}
