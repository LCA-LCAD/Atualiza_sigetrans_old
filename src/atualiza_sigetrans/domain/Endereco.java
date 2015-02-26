package atualiza_sigetrans.domain;

import atualiza_sigetrans.FuncoesEstaticas;
import atualiza_sigetrans.FuncoesEstaticas;
import atualiza_sigetrans.GeoCoderGoogle;
import atualiza_sigetrans.GeoCoderGoogle;
import com.vividsolutions.jts.geom.Point;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 *
 * @author Henrique
 */
@Embeddable
public class Endereco implements Serializable {

    @Transient
    private final double latitude = -24.955557;
    @Transient
    private final double longitude = -53.455195;
    private String rua;
    private String numero;
    @Column(columnDefinition = "varchar(11) DEFAULT ''")
    private String cep;
    //
    @Type(type = "org.hibernatespatial.GeometryUserType")
    private Point coordenadas;
    //
    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    private String bairro;
    @Column(columnDefinition = "varchar(100) DEFAULT ''")
    private String cidade;
    private String complemento;
    private Boolean cruzamento;
    private String cruzamentoCom;
    @Transient
    private String centroDoMapa;
    @Transient
    private MapModel map;

    public Endereco() {
        cruzamento = false;
        cidade = "";
        bairro = "";
        cep = "";
        rua = "";
        cruzamentoCom = "";
        numero = "";
        //Centro de Cascavel <Mudar isso para ser dinamico no futuro> Escolher a cidade!
        map = new DefaultMapModel();
        Marker marker = new Marker(new LatLng(this.latitude, this.longitude));
        marker.setDraggable(true);
        map.addOverlay(marker);
        centroDoMapa = "" + this.latitude + "," + this.longitude;
    }

    public Endereco(String rua, String numero, String cep, Point coordenadas, String bairro, String Cidade) {
        this.rua = rua;
        this.numero = numero;
        this.cep = cep;
        this.coordenadas = coordenadas;
        this.bairro = bairro;
        this.cidade = Cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public Point getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(Point coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String Cidade) {
        this.cidade = Cidade;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public boolean isCruzamento() {
        return cruzamento;
    }

    public void setCruzamento(boolean cruzamento) {
        this.cruzamento = cruzamento;
    }

    public String getCruzamentoCom() {
        return cruzamentoCom;
    }

    public void setCruzamentoCom(String cruzamentoCom) {
        this.cruzamentoCom = cruzamentoCom;
    }

    public String getCentroDoMapa() {
        return centroDoMapa;
    }

    public void setCentroDoMapa(String centroDoMapa) {
        this.centroDoMapa = centroDoMapa;
    }

    public Boolean getCruzamento() {
        return cruzamento;
    }

    public void setCruzamento(Boolean cruzamento) {
        this.cruzamento = cruzamento;
    }

    public MapModel getMap() {
        return map;
    }

    public void setMap(MapModel map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "Rua: " + rua + " numero: " + numero + " cep: " + cep + " bairro: " + bairro + " cidade: " + cidade;
    }

    public void updateCoordinates(Marker marker) {
        LatLng newLatLng = marker.getLatlng();
        String address = newLatLng.getLat() + "," + newLatLng.getLng();
        Endereco endereco = GeoCoderGoogle.queryLatLng(address);
        centroDoMapa = endereco.getCoordenadas().getX() + "," + endereco.getCoordenadas().getY();
        this.rua = endereco.getRua();
        this.numero = endereco.getNumero();
        this.bairro = endereco.getBairro();
        this.cidade = endereco.getCidade();
        this.cep = endereco.getCep();
        this.coordenadas = endereco.getCoordenadas();
        map = new DefaultMapModel();
        map.addOverlay(marker);
    }

    /**
     *
     */
    public void queryAddress() {
        //Prepara o endereço para ser buscado [remove acentos e concatena com o numero]
        String formatedString = FuncoesEstaticas.replaceSpecial(this.rua);
        String address = formatedString;

        if (!"".equals(address)) {
            if (!"".equals(this.numero)) {
                address = this.numero + "+" + formatedString;
            }
            //efetua a busca
            Endereco endereco;
            if ((endereco = GeoCoderGoogle.queryAddress(address)) != null) {
                this.rua = endereco.getRua();
                this.numero = endereco.getNumero();
                this.bairro = endereco.getBairro();
                this.cidade = endereco.getCidade();
                this.cep = endereco.getCep();
                this.coordenadas = endereco.getCoordenadas();
                map = new DefaultMapModel();
                Marker marker = new Marker(new LatLng(endereco.coordenadas.getX(), endereco.coordenadas.getY()));
                marker.setDraggable(true);
                map.addOverlay(marker);
                centroDoMapa = this.coordenadas.getX() + "," + this.coordenadas.getY();
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Endereço não encontrado", "Caso tenha certeza do endereço informado, preencha os dados manualmente"));
            }
        }
    }

    /**
     *
     */
    public void queryStringGeo() {
        String result = FuncoesEstaticas.replaceSpecial(this.cruzamentoCom);
        Endereco endereco;
        if ((endereco = GeoCoderGoogle.queryAddress(result)) != null) {
            this.cruzamentoCom = endereco.getRua();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Endereço informado não encontrado",
                    "para mais informações sobre consultas, veja o manual"));
        }
    }

    /**
     * Atualiza o modelo do mapa, colocando um marcador no local da coordenada.
     *
     * @return True caso o marcador seja um valor personalizado e false caso o
     * valor seja o padrão (centro da cidade).
     */
    public boolean updateMap(boolean markerDraggable) {
        map = new DefaultMapModel();
        if (coordenadas != null) {
            Marker marker = new Marker(new LatLng(coordenadas.getX(), coordenadas.getY()));
            marker.setDraggable(markerDraggable);
            map.addOverlay(marker);
            centroDoMapa = "" + coordenadas.getX() + "," + coordenadas.getY();
            return true;
        } else {
            Marker marker = new Marker(new LatLng(this.latitude, this.longitude));
            marker.setDraggable(markerDraggable);
            map.addOverlay(marker);
            centroDoMapa = "" + this.latitude + "," + this.longitude;
            return false;
        }
    }
}
