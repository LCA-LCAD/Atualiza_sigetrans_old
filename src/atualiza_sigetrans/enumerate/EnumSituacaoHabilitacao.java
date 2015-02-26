package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumSituacaoHabilitacao {

    REGULAR("Regular"),
    CACADA("Caçada"),
    SUSPENSA("Suspensa"),
    VENCIDA("Vencida"),
    NAO_INFORMADO("Não informado");
    //
    private String descricao;

    private EnumSituacaoHabilitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
