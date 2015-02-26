package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumEstadoCondutor {

    NAO_INFORMADO("Não informado"),
    SEM_FERIMENTOS("Sem ferimentos"),
    COM_FERIMENTOS("Com ferimentos"),
    OBITO_LOCAL("Óbito no local"),
    OBITO_POSTERIOR("Óbito posterior"),
    SINTOMAS_EMBRIAGUEZ("Sintomas de embriaguez ou entorpecentes");
    private String descricao;

    private EnumEstadoCondutor(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
