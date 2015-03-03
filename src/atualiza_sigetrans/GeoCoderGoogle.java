package atualiza_sigetrans;

import atualiza_sigetrans.domain.Endereco;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GeoCoderGoogle {

    /**
     * @param address endereço a ser buscado
     * @return coordenadas (lat/lng) do endereço buscado.
     */
    public static Endereco queryAddress(String address) {
        if (!"".equals(address)) {
//            System.out.println(address);
            //Prepara a url de consulta substituindo virgulas e espaços por +
            address = address.replace(",", "+");
            address = address.replace(" ", "+");
            //Concatena com Cascavel para evitar conflito por exemplo rua paraná em Foz do Iguaçu
            address += "+cascavel+pr";
            try {
                //Monta a url de serviço geocoding
                URL url = new URL("http://maps.google.com/maps/api/geocode/xml?address=" + address + "&sensor=false");
                //Efetua a consulta
//                System.out.println(url);
                Endereco end = processQuery(url);
                //
                if (end.getRua() == null) {
                    return null;
                } else {
                    return end;
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(GeoCoderGoogle.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("query string toGeoCoder is null. Return value default");
            Endereco end = new Endereco();
            return null;
        }
        return null;
    }

    public static Endereco queryLatLng(String coordenadas) {
        if (!"".equals(coordenadas)) {
            try {
                URL url = new URL("http://maps.google.com/maps/api/geocode/xml?latlng=" + coordenadas + "&sensor=false");
                Endereco end = processQuery(url);
                if (end.getRua() == null) {
                    return null;
                } else {
                    return end;
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(GeoCoderGoogle.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("query string to GeoCoder is null. Return value default");
            return null;
        }
        return null;
    }

    public static void configureProxy(String proxy, String porta) {
        System.setProperty("ProxySet", "true");
        System.setProperty("http.proxyHost", proxy);
        System.setProperty("http.proxyPort", porta);
        System.setProperty("http.proxyUser", "gabriel.silva9");
        System.setProperty("http.proxyPassword","A96b73e");
        //System.setProperty("", porta)
    }

    private static Endereco processQuery(URL url) {
        //Efetua a consulta através de uma solicitação http
//        configureProxy("proxy.unioeste.br", "8080");
        System.out.println("Mandando a url " + url.toString());
        try {
            Endereco end = new Endereco();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Document geocoderResultDocument = null;

            try {
//                conn.setRequestMethod("GET");
                conn.connect();
                InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());
                geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);

            } catch (SAXException | ParserConfigurationException ex) {
                Logger.getLogger(GeoCoderGoogle.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                conn.disconnect();
            }
            //Recebe o resultado em formato XML e extrai os dados recebidos
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList resultNodeList;
            NodeList nodeList;

            // a) obtain the formatted_address field for every result 
//            System.out.println("Query is null "+geocoderResultDocument == null);
//            String local = (String) xpath.evaluate("/GeocodeResponse/result/formatted_address", geocoderResultDocument);
//            Logger.getLogger("SIGETRANS").log(Level.INFO, "Geocoding -> {0}", local);

            // c) extract the coordinates of the first result
            nodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component/*", geocoderResultDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                String nodeName = node.getNodeName();

                if (nodeName.equals("type")) {
                    switch (node.getTextContent()) {
                        case "street_number":
                            end.setNumero(nodeList.item(i - 2).getTextContent());
                            break;
                        case "route":
                            end.setRua(nodeList.item(i - 2).getTextContent());
                            break;
                        case "sublocality":
                            end.setBairro(nodeList.item(i - 2).getTextContent());
                            break;
                        case "locality":
                            end.setCidade(nodeList.item(i - 2).getTextContent());
                            break;
                        case "postal_code":
                            end.setCep(nodeList.item(i - 2).getTextContent());
                            break;
                        default:
                        //nothing
                    }
                }
            }

            // c) extract the coordinates of the first result
            resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);

            double lat = 0;
            double lng = 0;

            for (int i = 0; i < resultNodeList.getLength(); ++i) {
                Node node = resultNodeList.item(i);
                if ("lat".equals(node.getNodeName())) {
                    lat = Double.parseDouble(node.getTextContent());
                }
                if ("lng".equals(node.getNodeName())) {
                    lng = Double.parseDouble(node.getTextContent());
                }
            }
            //Define uma geometria do PostGIS
            end.setCoordenadas(new Point(new Coordinate(lat, lng), new PrecisionModel(), 4326));

            return end;
        } catch (XPathExpressionException | IOException ex) {
            Logger.getLogger(GeoCoderGoogle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Endereco();
    }
}
