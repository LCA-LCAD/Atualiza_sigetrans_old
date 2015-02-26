package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumGrauAvarias {

    SEM_AVARIAS("Sem avarias"),
    PEQUENA("Pequena"),
    MEDIA("Media"),
    GRANDE("Grande"),
    NAO_INFORMADO("Não informado");
    //
    private String descricao;

    private EnumGrauAvarias(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
