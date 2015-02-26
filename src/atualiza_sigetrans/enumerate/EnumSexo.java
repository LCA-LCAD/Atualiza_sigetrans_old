package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumSexo implements InterfaceEnum {

    MASCULINO("Masculino"),
    FEMININO("Feminino"),
    NAO_INFORMADO("Não informado");
    private String descricao;

    private EnumSexo(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
