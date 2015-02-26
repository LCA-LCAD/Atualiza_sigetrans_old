package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
//TODO: Retirar esse tipo se a escala de glasgow for somente numérica msm...
public enum EnumRespostaVerbal {

    ORIENTADO("Orientado"),
    CONFUSO("Confuso"),
    DESCONEXO("Desconexo"),
    INCOMPREENSIVEL("Incompreensível"),
    AUSENTE("Ausente");
    private String descricao;

    private EnumRespostaVerbal(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
