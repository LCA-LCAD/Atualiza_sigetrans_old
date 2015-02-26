package atualiza_sigetrans.enumerate;


/**
 *
 * @author Henrique
 */
//TODO: Retirar esse tipo se a escala de glasgow for somente numérica msm...
public enum EnumAberturaOcular{

    ESPONTANEA("Espontânea"),
    A_VOZ("À voz"),
    A_DOR("À dor"),
    AUSENTE("Ausente");
    //
    private String descricao;

    private EnumAberturaOcular(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
