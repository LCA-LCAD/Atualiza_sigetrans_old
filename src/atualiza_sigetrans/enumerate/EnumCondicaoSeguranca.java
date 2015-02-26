package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumCondicaoSeguranca implements InterfaceEnum{

    USAVA_CINTO("Usava cinto"),
    USAVA_CAPACETE("Usava capacete"),
    CADEIRINHA("Cadeirinha de segurança"),
    SEM_CAPACETE("Sem capacate"),
    SEM_CINTO("Sem cinto"),
    NAO_EXIGIVEL("Não exigível"),
    NAO_OBSERVADO("Não Observado");
    //
    private String descricao;

    private EnumCondicaoSeguranca(String descricao) {
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
