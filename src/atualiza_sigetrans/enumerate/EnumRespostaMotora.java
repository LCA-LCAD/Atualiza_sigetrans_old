package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
//TODO: Retirar esse tipo se a escala de glasgow for somente numérica msm...
public enum EnumRespostaMotora {

    RESPONDE_COMANDOS("Responde à comandos"),
    APROPRIADA_A_DOR("Apropriada à dor"),
    RETIRADA_A_DOR("Retirada à dor"),
    FLEXAO("Flexão"),
    EXTENSAO("Extensão"),
    AUSENTE("Ausente");
    private String descricao;

    private EnumRespostaMotora(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
