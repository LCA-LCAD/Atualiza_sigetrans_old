package atualiza_sigetrans.enumerate;

/**
 *
 * @author Leonardo Merlin <leonardo.merlin at unioeste.br>
 */
public enum EnumEscolaridade implements InterfaceEnum {

    PRIMEIRO_GRAU_COMPLETO("Primeiro grau completo"),
    PRIMEIRO_GRAU_INCOMPLETO("Primeiro grau incompleto"),
    SEGUNDO_GRAU_COMPLETO("Segundo grau completo"),
    SEGUNDO_GRAU_INCOMPLETO("Segundo grau incompleto"),
    TERCEIRO_GRAU_COMPLETO("Terceiro grau completo"),
    TERCEIRO_GRAU_INCOMPLETO("Terceiro grau incompleto"),
    POS_GRADUACAO("Pós graduação"),
    NAO_INFORMADO("Não informado");
    //
    private String descricao;

    private EnumEscolaridade(String descricao) {
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
