package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumCategoriaHabilitacao{

    A("A"),
    B("B"), 
    AB("AB"),
    C("C"), 
    AC("AC"),
    D("D"), 
    AD("AD"),
    E("E"), 
    AE("AE"),
    NAO_INFORMADO("Não informado");
    
    private String descricao;

    private EnumCategoriaHabilitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    
}
