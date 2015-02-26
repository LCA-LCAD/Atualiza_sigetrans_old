package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumEscalaComa {

    _0("0"),
    _4_a_5("4 e 5"),
    _6_a_8("6 - 8"),
    _9_a_12("9 - 12"),
    _13_a_15("13 - 15");
    //
    private String descricao;

    private EnumEscalaComa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
