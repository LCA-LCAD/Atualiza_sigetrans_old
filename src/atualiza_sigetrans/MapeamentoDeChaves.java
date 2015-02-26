package atualiza_sigetrans;

/**
 *
 * @author Servidor
 */
public class MapeamentoDeChaves {

    private Long id_ocorrencia;
    private Long id_Ras;
    private Long cod_rgo;

    public MapeamentoDeChaves(Long id_ocorrencia, Long id_Ras, Long cod_rgo) {
        this.id_ocorrencia = id_ocorrencia;
        this.id_Ras = id_Ras;
        this.cod_rgo = cod_rgo;
    }

    public Long getCod_rgo() {
        return cod_rgo;
    }

    public void setCod_rgo(Long cod_rgo) {
        this.cod_rgo = cod_rgo;
    }

    public Long getId_ocorrencia() {
        return id_ocorrencia;
    }

    public void setId_ocorrencia(Long id_ocorrencia) {
        this.id_ocorrencia = id_ocorrencia;
    }

    public Long getId_Ras() {
        return id_Ras;
    }

    public void setId_Ras(Long id_Ras) {
        this.id_Ras = id_Ras;
    }
}
