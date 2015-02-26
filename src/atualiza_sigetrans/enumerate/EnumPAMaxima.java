
package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumPAMaxima {

    _0("0"),
    _1_a_49("1 a 49"),
    _50_a_75("50 a 75"),
    _76_a_89("76 a 89"),
    MAIOR_QUE_89("+89");
    private String descricao;

    private EnumPAMaxima(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
