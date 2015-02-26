package atualiza_sigetrans;

/**
 *
 * @author SERVIDOR
 */
public class TempVeiculo {

    private long rgo_veiculo;
    private String placa;
    private String municipio;
    private String condutor;
    private String tipoVeiculo;

    public TempVeiculo(long rgo_v,String placa, String municipio, String condutor, String tipoVeiculo) {
        rgo_veiculo = rgo_v;
        this.placa = placa;
        this.municipio = municipio;
        this.condutor = condutor;
        this.tipoVeiculo = tipoVeiculo;
    }

    public long getRgo_veiculo() {
        return rgo_veiculo;
    }

    public void setRgo_veiculo(long rgo_veiculo) {
        this.rgo_veiculo = rgo_veiculo;
    }

    public TempVeiculo() {
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getCondutor() {
        return condutor;
    }

    public void setCondutor(String condutor) {
        this.condutor = condutor;
    }

    public String getTipoVeiculo() {
        return tipoVeiculo;
    }

    public void setTipoVeiculo(String tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }
}
