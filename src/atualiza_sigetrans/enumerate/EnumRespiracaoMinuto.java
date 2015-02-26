/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumRespiracaoMinuto {

    _0("0"),
    _1_a_5("1 a 5"),
    _6_a_10("6 a 10"),
    _10_a_29("10 a 29"),
    MAIOR_QUE_29("+29");
    private String descricao;

    private EnumRespiracaoMinuto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
