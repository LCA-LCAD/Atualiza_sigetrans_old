package atualiza_sigetrans.domain;

import atualiza_sigetrans.enumerate.EnumCategoriaHabilitacao;
import atualiza_sigetrans.enumerate.EnumSituacaoHabilitacao;
import atualiza_sigetrans.enumerate.EnumTipoHabilitacao;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;

/**
 *
 * @author Leonardo Merlin <leonardo.merlin at unioeste.br>
 */
@Embeddable
public class Habilitacao implements Serializable {

    @Enumerated(EnumType.STRING)
    private EnumTipoHabilitacao tipoHabilitacao;
    @Enumerated(EnumType.STRING)
    private EnumCategoriaHabilitacao categoriaHabilitacao;
    @Enumerated(EnumType.STRING)
    private EnumSituacaoHabilitacao situacaoHabilitacao;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataValidadeHabilitacao;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataPrimeiraHabilitacao;

    public Habilitacao() {
        tipoHabilitacao = EnumTipoHabilitacao.NAO_INFORMADO;
        categoriaHabilitacao = EnumCategoriaHabilitacao.NAO_INFORMADO;
        situacaoHabilitacao = EnumSituacaoHabilitacao.NAO_INFORMADO;
    }

    public Date getDataPrimeiraHabilitacao() {
        return dataPrimeiraHabilitacao;
    }

    public void setDataPrimeiraHabilitacao(Date dataPrimeiraHabilitacao) {
        this.dataPrimeiraHabilitacao = dataPrimeiraHabilitacao;
    }

    public Date getDataValidadeHabilitacao() {
        return dataValidadeHabilitacao;
    }

    public void setDataValidadeHabilitacao(Date dataValidadeHabilitacao) {
        this.dataValidadeHabilitacao = dataValidadeHabilitacao;
    }

    public EnumCategoriaHabilitacao getCategoriaHabilitacao() {
        return categoriaHabilitacao;
    }

    public void setCategoriaHabilitacao(EnumCategoriaHabilitacao categoriaHabilitacao) {
        this.categoriaHabilitacao = categoriaHabilitacao;
    }

    public EnumSituacaoHabilitacao getSituacaoHabilitacao() {
        return situacaoHabilitacao;
    }

    public void setSituacaoHabilitacao(EnumSituacaoHabilitacao situacaoHabilitacao) {
        this.situacaoHabilitacao = situacaoHabilitacao;
    }

    public EnumTipoHabilitacao getTipoHabilitacao() {
        return tipoHabilitacao;
    }

    public void setTipoHabilitacao(EnumTipoHabilitacao tipoHabilitacao) {
        this.tipoHabilitacao = tipoHabilitacao;
    }
}