package atualiza_sigetrans.domain;

import atualiza_sigetrans.enumerate.EnumGrauAvarias;
import atualiza_sigetrans.enumerate.EnumUnidadeFederativa;
import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Henrique
 */
@Entity
public class Veiculo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String placa;
    private String cidade;
    @Enumerated(EnumType.STRING)
    private EnumUnidadeFederativa estado;
    @Enumerated(EnumType.STRING)
    private EnumGrauAvarias grauAvarias;
    private String condicaoPneus;
    private String comoVeiculoEstava;
    private String sentidoVeiculoTrafegava;

    public Veiculo() {
    }

    public Veiculo(Long id, String placa, String cidade, EnumUnidadeFederativa estado, String condicaoPneus, String comoVeiculoEstava, String sentidoVeiculoTrafegava, EnumGrauAvarias grauAvarias) {
        this.id = id;
        this.placa = placa;
        this.cidade = cidade;
        this.estado = estado;
        this.condicaoPneus = condicaoPneus;
        this.comoVeiculoEstava = comoVeiculoEstava;
        this.sentidoVeiculoTrafegava = sentidoVeiculoTrafegava;
        this.grauAvarias = grauAvarias;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getComoVeiculoEstava() {
        return comoVeiculoEstava;
    }

    public void setComoVeiculoEstava(String comoVeiculoEstava) {
        this.comoVeiculoEstava = comoVeiculoEstava;
    }

    public String getCondicaoPneus() {
        return condicaoPneus;
    }

    public void setCondicaoPneus(String condicaoPneus) {
        this.condicaoPneus = condicaoPneus;
    }

    public EnumUnidadeFederativa getEstado() {
        return estado;
    }

    public void setEstado(EnumUnidadeFederativa estado) {
        this.estado = estado;
    }

    public EnumGrauAvarias getGrauAvarias() {
        return grauAvarias;
    }

    public void setGrauAvarias(EnumGrauAvarias grauAvarias) {
        this.grauAvarias = grauAvarias;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getSentidoVeiculoTrafegava() {
        return sentidoVeiculoTrafegava;
    }

    public void setSentidoVeiculoTrafegava(String sentidoVeiculoTrafegava) {
        this.sentidoVeiculoTrafegava = sentidoVeiculoTrafegava;
    }
}
