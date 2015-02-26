package atualiza_sigetrans.domain;

import atualiza_sigetrans.enumerate.EnumCondicaoSeguranca;
import atualiza_sigetrans.enumerate.EnumEscolaridade;
import atualiza_sigetrans.enumerate.EnumPosicaoNoVeiculo;
import atualiza_sigetrans.enumerate.EnumSituacaoCivil;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author Henrique
 */
@Entity
@Table(name = "Envolvido")
public class Envolvido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private VitimaRAS vitimaRAS;
    
    @OneToOne(optional=true)
    private Condutor condutor;
    
    @OneToOne
    @ForeignKey(name = "FK_PESSOA")
    private Pessoa pessoa;
    @ManyToOne
    @ForeignKey(name = "FK_ENVOLVIDO", inverseName = "FK_OCORRENCIA")
    private Ocorrencia ocorrencia;
    private String tipoVeiculo;
    private String profissao;
    private String situacao;
    private Integer idadeAnos;
    private Boolean acidenteDeTrabalho;
    private Boolean ativo;
    @Enumerated(EnumType.STRING)
    private EnumPosicaoNoVeiculo posicaoNoVeiculo;
    @Enumerated(EnumType.STRING)
    private EnumEscolaridade escolaridade;
    @Enumerated(EnumType.STRING)
    private EnumSituacaoCivil estadoCivil;
    @Enumerated(EnumType.STRING)
    private EnumCondicaoSeguranca condicaoSeguranca;
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'envolvido'")
    private String tipoRegistro;  
    @Embedded
    private ControleUsuario controleUsuario;

    public Envolvido() {
        this.pessoa = new Pessoa();
        this.posicaoNoVeiculo = EnumPosicaoNoVeiculo.NAO_INFORMADO;
        this.ocorrencia = new Ocorrencia();
        this.condutor = new Condutor();
        this.controleUsuario = new ControleUsuario();
        this.tipoRegistro = "envolvido";
        this.profissao = "Não informado";
        this.situacao = "Não informado";
        this.tipoVeiculo = "Não informado";
        this.ativo = true;
    }

    public Envolvido(Pessoa pessoa) {
        this.pessoa = pessoa;
        this.posicaoNoVeiculo = EnumPosicaoNoVeiculo.NAO_INFORMADO;
        this.ocorrencia = new Ocorrencia();
        this.tipoRegistro = "envolvido";
        this.ativo = true;
    }

    public Envolvido(Envolvido envolvido) {
        this();
        this.pessoa = envolvido.pessoa;
        this.posicaoNoVeiculo = envolvido.getPosicaoNoVeiculo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VitimaRAS getVitimaRAS() {
        return vitimaRAS;
    }

    public void setVitimaRAS(VitimaRAS vitimaRAS) {
        this.vitimaRAS = vitimaRAS;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public String getTipoVeiculo() {
        return tipoVeiculo;
    }

    public void setTipoVeiculo(String tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public Integer getIdadeAnos() {
        return idadeAnos;
    }

    public void setIdadeAnos(Integer idadeAnos) {
        this.idadeAnos = idadeAnos;
    }

    public Boolean getAcidenteDeTrabalho() {
        return acidenteDeTrabalho;
    }

    public void setAcidenteDeTrabalho(Boolean acidenteDeTrabalho) {
        this.acidenteDeTrabalho = acidenteDeTrabalho;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public EnumPosicaoNoVeiculo getPosicaoNoVeiculo() {
        return posicaoNoVeiculo;
    }

    public void setPosicaoNoVeiculo(EnumPosicaoNoVeiculo posicaoNoVeiculo) {
        this.posicaoNoVeiculo = posicaoNoVeiculo;
    }

    public EnumEscolaridade getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(EnumEscolaridade escolaridade) {
        this.escolaridade = escolaridade;
    }

    public EnumSituacaoCivil getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(EnumSituacaoCivil estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public EnumCondicaoSeguranca getCondicaoSeguranca() {
        return condicaoSeguranca;
    }

    public void setCondicaoSeguranca(EnumCondicaoSeguranca condicaoSeguranca) {
        this.condicaoSeguranca = condicaoSeguranca;
    }

    public String getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(String tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public ControleUsuario getControleUsuario() {
        return controleUsuario;
    }

    public void setControleUsuario(ControleUsuario controleUsuario) {
        this.controleUsuario = controleUsuario;
    }

    public Boolean hasCondutor() {
        return posicaoNoVeiculo.equals(EnumPosicaoNoVeiculo.CONDUTOR);
    }

    public Condutor getCondutor() {
        return condutor;
    }

    public void setCondutor(Condutor condutor) {
        this.condutor = condutor;
    }

    public boolean hasRAS() {
        return vitimaRAS == null ? false : true;
    }

    public boolean notHasRAS() {
        return vitimaRAS == null ? true : false;
    }
}
