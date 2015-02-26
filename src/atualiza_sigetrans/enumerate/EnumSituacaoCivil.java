package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumSituacaoCivil {

    CASADO("Casado"),
    SOLTEIRO("Solteiro"),
    DIVORCIADO("Divorciado"),
    UNIAO_ESTAVEL("União estável"),
    VIUVO("Viuvo"),
    NAO_INFORMADO("Não informado");
    //
    private String descricao;

    private EnumSituacaoCivil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
