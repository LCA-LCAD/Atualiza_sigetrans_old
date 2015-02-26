package atualiza_sigetrans.domain;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Henrique
 */
@Embeddable
public class Lesao implements Serializable {

    @Column(columnDefinition = "Text")
    private String cranio;
    @Column(columnDefinition = "Text")
    private String face;
    @Column(columnDefinition = "Text")
    private String dorso;
    @Column(columnDefinition = "Text")
    private String pelvis;
    @Column(columnDefinition = "Text")
    private String torax;
    @Column(columnDefinition = "Text")
    private String abdomen;
    @Column(columnDefinition = "Text")
    private String pescoco;
    @Column(columnDefinition = "Text")
    private String regiao_pelvica;
    @Column(columnDefinition = "Text")
    private String membro_sup_direito;
    @Column(columnDefinition = "Text")
    private String membro_sup_esquerdo;
    @Column(columnDefinition = "Text")
    private String membro_inf_direito;
    @Column(columnDefinition = "Text")
    private String membro_inf_esquerdo;

    public Lesao() {
        cranio = "";
        abdomen = "";
        dorso = "";
        face = "";
        membro_inf_direito = "";
        membro_inf_esquerdo = "";
        membro_sup_direito = "";
        membro_sup_esquerdo = "";
        pelvis = "";
        pescoco = "";
        regiao_pelvica = "";
        torax = "";

    }

    public Lesao(String cranio, String face, String dorso, String pelvis, String torax, String abdomen, String pescoco, String regiao_pelvica, String membro_sup_direito, String membro_sup_esquerdo, String membro_inf_direito, String membro_inf_esquerdo) {
        this.cranio = cranio;
        this.face = face;
        this.dorso = dorso;
        this.pelvis = pelvis;
        this.torax = torax;
        this.abdomen = abdomen;
        this.pescoco = pescoco;
        this.regiao_pelvica = regiao_pelvica;
        this.membro_sup_direito = membro_sup_direito;
        this.membro_sup_esquerdo = membro_sup_esquerdo;
        this.membro_inf_direito = membro_inf_direito;
        this.membro_inf_esquerdo = membro_inf_esquerdo;
    }

    public String getCranio() {
        return cranio;
    }

    public void setCranio(String cranio) {
        this.cranio = cranio;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getDorso() {
        return dorso;
    }

    public void setDorso(String dorso) {
        this.dorso = dorso;
    }

    public String getPelvis() {
        return pelvis;
    }

    public void setPelvis(String pelvis) {
        this.pelvis = pelvis;
    }

    public String getTorax() {
        return torax;
    }

    public void setTorax(String torax) {
        this.torax = torax;
    }

    public String getAbdomen() {
        return abdomen;
    }

    public void setAbdomen(String abdomen) {
        this.abdomen = abdomen;
    }

    public String getPescoco() {
        return pescoco;
    }

    public void setPescoco(String pescoco) {
        this.pescoco = pescoco;
    }

    public String getRegiao_pelvica() {
        return regiao_pelvica;
    }

    public void setRegiao_pelvica(String regiao_pelvica) {
        this.regiao_pelvica = regiao_pelvica;
    }

    public String getMembro_sup_direito() {
        return membro_sup_direito;
    }

    public void setMembro_sup_direito(String membro_sup_direito) {
        this.membro_sup_direito = membro_sup_direito;
    }

    public String getMembro_sup_esquerdo() {
        return membro_sup_esquerdo;
    }

    public void setMembro_sup_esquerdo(String membro_sup_esquerdo) {
        this.membro_sup_esquerdo = membro_sup_esquerdo;
    }

    public String getMembro_inf_direito() {
        return membro_inf_direito;
    }

    public void setMembro_inf_direito(String membro_inf_direito) {
        this.membro_inf_direito = membro_inf_direito;
    }

    public String getMembro_inf_esquerdo() {
        return membro_inf_esquerdo;
    }

    public void setMembro_inf_esquerdo(String membro_inf_esquerdo) {
        this.membro_inf_esquerdo = membro_inf_esquerdo;
    }
}
