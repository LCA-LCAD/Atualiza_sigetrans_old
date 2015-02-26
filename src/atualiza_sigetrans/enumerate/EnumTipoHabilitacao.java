package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumTipoHabilitacao {

    NAO_INFORMADO("Não informado"),
    HABILITADO("Habilitado"),
    INABILITADO("Inabilitado"),
    PERMISSAO("Permissao"),
    NAO_EXIGIVEL("Não exigível");
    //
    private String descricao;

    private EnumTipoHabilitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
