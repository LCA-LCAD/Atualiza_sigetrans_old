        package atualiza_sigetrans.enumerate;

/**
 *
 * @author Henrique
 */
public enum EnumPosicaoNoVeiculo implements InterfaceEnum  {

    NAO_INFORMADO("Não informado"),
    CONDUTOR("Condutor"),
    GARUPA("Garupa"),
    BANCO_DIANTEIRO("Banco dianteiro"),
    BANCO_TRASEIRO("Banco traseiro");
    private String descricao;

    private EnumPosicaoNoVeiculo(String descricao) {
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
