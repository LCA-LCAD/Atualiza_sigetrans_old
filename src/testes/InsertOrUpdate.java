/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import static atualiza_sigetrans.Atualiza_sigetrans.verificaEnvolvidoAtivo;
import atualiza_sigetrans.FuncoesEstaticas;
import atualiza_sigetrans.GeoCoderGoogle;
import atualiza_sigetrans.MapeamentoDeChaves;
import atualiza_sigetrans.TempVeiculo;
import atualiza_sigetrans.TipoProfissao;
import atualiza_sigetrans.domain.Endereco;
import atualiza_sigetrans.enumerate.EnumAberturaOcular;
import atualiza_sigetrans.enumerate.EnumCondicaoSeguranca;
import atualiza_sigetrans.enumerate.EnumEscalaComa;
import atualiza_sigetrans.enumerate.EnumEscolaridade;
import atualiza_sigetrans.enumerate.EnumPAMaxima;
import atualiza_sigetrans.enumerate.EnumPosicaoNoVeiculo;
import atualiza_sigetrans.enumerate.EnumRespiracaoMinuto;
import atualiza_sigetrans.enumerate.EnumRespostaMotora;
import atualiza_sigetrans.enumerate.EnumRespostaVerbal;
import atualiza_sigetrans.enumerate.EnumSexo;
import atualiza_sigetrans.enumerate.EnumSituacaoCivil;
import com.vividsolutions.jts.geom.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Administrador
 */
public class InsertOrUpdate {

    public static int totalRegistrosGeo = 0;
    public static int totalRegistrosRGOLidos = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception, PSQLException {
        List metadata = new ArrayList();
        Long id_ras = null;
        int count_max = 0;
        Long id_ocorrencia = null;
        boolean envolvidoAtivo = false;
        List<MapeamentoDeChaves> listOcorrencias = new ArrayList<>();

        try {
            System.out.println("Conectado aos bombeiros?   " + Atualiza.Conecta_bombeiros());
            System.out.println("Conectado ao sigetrans?   " + Atualiza.Conecta_sigetrans());
        } catch (Exception ex) {
            System.out.println("Nao conectou!");
            System.exit(2);
        }
        ResultSet date = Atualiza.Consulta_sigetrans("select max(dataacidente) as data from formularios.ras where usuario_login='SISTEMA'");
        date.next();
        String Max_date = date.getString("data");
        date.close();

        //Configuracoes de acesso e armazenamento da tabela export_metadata(Bombeiros)        
        Calendar cal = Calendar.getInstance();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
//        String query = "SELECT * FROM sysbmccb.sigetrans_export_metadata where ts>'" + time + "'";
        String query = "SELECT * FROM sysbmccb.sigetrans_export_metadata where ts>='" + Max_date + "'";

//        String query = "SELECT * FROM sysbmccb.sigetrans_export_metadata where ts>'2014-06-01'";
        ResultSet Result_sigetrans_metadata = Atualiza.Consulta_bombeiros(query);
        System.out.println(query);

        while (Result_sigetrans_metadata.next()) {
            Linha_sigetrans a = new Linha_sigetrans();
            a.setId(Integer.parseInt(Result_sigetrans_metadata.getString("id")));
            a.setKs(Result_sigetrans_metadata.getString("ks"));
            a.setOper(Result_sigetrans_metadata.getString("oper"));
            metadata.add(a);
        }

        //Implementar metodo de atualização por insert e update
        while (count_max < metadata.size()) {
            Linha_sigetrans bombeiros = (Linha_sigetrans) metadata.get(count_max);
            String[] result = bombeiros.getKs().split(",");
            String Ras = "select * from formularios.ras where rgo=" + result[1];
            ResultSet ras = null;
            try {
                ras = Atualiza.Consulta_sigetrans(Ras);
            } catch (Exception ex) {
                System.out.println("Erro no ResultSet RAS");
            }

            String Consult_bombeiros = "select cod_rgo, datahora,log_bairro.BAI_NO as bairro,endereco_geral.log_nome,endereco_geral.CEP as cep,endereco_geral.rua as rua,referencia,tab_ocorrencia.numendereco as numero, endereco_cruzamento.log_nome as cruzamento,log_localidade.LOC_NOSUB as cidade, descr_ocorrencia, ocorrencia, nome as causa, vitima as possui_vitima "
                    + " from (select cod_fracao, cod_rgo, datahora, tipoocorrencia, descr_ocorrencia,cod_municipio, endereco, esquina,bairro,referencia, cod_causa, vitima,numendereco from rgo where cod_rgo=" + result[1] + ") as tab_ocorrencia "
                    + "inner join (select cod_ocorrencia, ocorrencia from ts_ocorrencia where quadroccb='Acidentes de trânsito') as tab_tp_ocorrencia on (tipoocorrencia=cod_ocorrencia) "
                    + "inner join (select cod_causa, nome from ts_causa) as tab_causa using(cod_causa) "
                    + "inner join log_bairro on log_bairro.ID_BAIRRO=tab_ocorrencia.bairro  "
                    + "inner join log_localidade on log_localidade.ID_LOCALIDADE=tab_ocorrencia.cod_municipio "
                    + "inner join (select id_logradouro,CEP,CONCAT(LOG_TIPO_LOGRADOURO,' ',LOG_NO) as rua, log_nome log_no_sem_acento, log_nome from log_logradouro) as endereco_geral on (endereco_geral.id_logradouro=endereco)"
                    + "left outer join (select id_logradouro, log_nome log_no_sem_acento, log_nome from log_logradouro) as endereco_cruzamento on (endereco_cruzamento.id_logradouro=esquina)";
            ResultSet rs = null;
            try {
                rs = Atualiza.Consulta_bombeiros(Consult_bombeiros);
            } catch (Exception ex) {
                System.out.println("Erro no ResultSet rs");
            }

            //********************************INSERT******************************
            if (bombeiros.getOper().equals("i")) {
                try {
                    System.out.println("INSERT - id " + bombeiros.getId());
                    if (!ras.next()) {

                        if (rs.next()) {
                            System.out.println("Cod RGO: " + rs.getString("cod_rgo"));
                            System.out.println("Total de ocorrencias: " + totalRegistrosRGOLidos);

                            String insertOcorrencia;
                            String cod_rgo = rs.getString("cod_rgo");
                            Date data = processDate(rs.getString("datahora"), true);
                            Date horario = processDate(rs.getString("datahora"), false);


                            /*Verifica a rua e tenta colocar no padrÃ£o google maps do SIGETRANS
                             Se o endereÃ§o nÃ£o contiver um nÃºmero aproximado, a consulta ao google maps serÃ¡ ignorada
                             e o endereÃ§o obtido na base dos bombeiros sera usado */
                            Endereco end = null;
                            if (isDigit(rs.getString("numero")) && Integer.parseInt(rs.getString("numero")) != 0) {
                                end = processaEndereco(rs.getString("rua"), rs.getString("numero"), rs.getString("cep"), rs.getString("bairro"), rs.getString("cidade"), rs.getString("cruzamento"));
                            } else {
                                end = processaEndereco(rs.getString("endereco_geral.log_nome"), rs.getString("cruzamento"), rs.getString("cep"), rs.getString("bairro"), rs.getString("cidade"));
                            }

                            /*Usar o replaceAll para campos que nÃ£o possuem valores prÃ©-definidos na base dos bombeiros
                             isso evita erros de inserÃ§Ã£o (Erros de SQL) por conter caracteres especiais*/
                            String observacoes = rs.getString("referencia").replaceAll("['<>\\|/]", " ");

                            String tipoAcidente = rs.getString("ocorrencia");

                            if (tipoAcidente.contains("-")) {
                                tipoAcidente = tipoAcidente.split("- ")[1];
                            }

                            String tipoCausa = rs.getString("causa").replaceAll("['<>\\|/]", " ");

                            //Verifica se existe o tipo no banco
                            ResultSet consulta = null;
                            try {
                                consulta = Atualiza.Consulta_sigetrans("SELECT descricao FROM tipocausa WHERE descricao=\'" + tipoCausa + "\'");
                            } catch (Exception ex) {
                                System.out.println("Erro no ResultSet consulta");
                            }

                            if (consulta.wasNull()) {
                                tipoCausa = "Não apurado";
                            }

                            String severidade;
                            if (rs.getString("possui_vitima").equals("Sim")) {
                                severidade = "Com vítimas";
                            } else {
                                severidade = "Sem vítimas";
                            }

                            totalRegistrosRGOLidos++;

                            String insertRAS;
                            //
                            if (end.getCoordenadas() != null) {
                                insertOcorrencia = preparaInsertOcorrencia(true);
                                insertRAS = preparaConsultaRAS(true);
                            } else {
                                insertOcorrencia = preparaInsertOcorrencia(false);
                                insertRAS = preparaConsultaRAS(false);
                            }

                            //InserÃ§Ã£o de ocorrencia
                            insertOcorrencia += "(\'" + new java.sql.Date(data.getTime()) + "\'";
                            insertOcorrencia += ", \'" + new java.sql.Time(horario.getTime()) + "\'";
                            insertOcorrencia += ", \'" + tipoAcidente + "\'"; // TipoAcidente
                            insertOcorrencia += ", \'Não informado\'"; // TipoClima
                            insertOcorrencia += ", \'Não informado\'"; // TipoConservacao
                            insertOcorrencia += ", \'Não informado\'"; // TipoPavimentacao
                            insertOcorrencia += ", \'Não informado\'"; // TipoPerfilPista
                            insertOcorrencia += ", \'" + tipoCausa + "\'"; // TipoCausa  (tinha que tratar a causa...)
                            insertOcorrencia += ", \'" + severidade + "\'"; // TipoSeveridade (tinha que vericar se tinha vitimas ou nÃ£o)
                            insertOcorrencia += ", \'Não informado\'"; // TipoSuperficie
                            insertOcorrencia += ", \'" + end.getBairro().replaceAll("['<>\\|/]", " ") + "\'"; // bairro
                            insertOcorrencia += ", \'" + end.getCep() + "\'";   // cep
                            insertOcorrencia += ", \'" + end.getCidade().replaceAll("['<>\\|/]", " ") + "\'";  // cidade
                            if (end.getCoordenadas() != null) {
                                insertOcorrencia += ", " + "ST_GeomFromText('" + end.getCoordenadas() + "',4326)";  // coordenadas
                            }
                            insertOcorrencia += ", \' " + end.getNumero() + "\'";  // numero
                            insertOcorrencia += ", \'" + end.getRua().replaceAll("['<>\\|/]", " ") + "\'";  // rua
                            insertOcorrencia += ", \'" + end.getCruzamento() + "\'";  // cruzamento (boolean)
                            insertOcorrencia += ", \'" + end.getCruzamentoCom().replaceAll("['<>\\|/]", " ") + "\'";  // cruzamentoCom (String)
                            insertOcorrencia += ", \'SISTEMA\'";  // Usuario criador (criado pelo sistema)
                            insertOcorrencia += ", \'" + new java.sql.Date(new Date().getTime()) + "\'";  // Data de criacao...
                            insertOcorrencia += ", \'" + observacoes + "\'";  // Observacao...
                            insertOcorrencia += ", \'Não informado\') returning id;";  // sinalizacoes...

                            ResultSet rsInsert = null;
                            try {
                                rsInsert = Atualiza.Consulta_sigetrans(insertOcorrencia);
                            } catch (Exception ex) {
                                System.out.println("Erro no ResultSet rsInsert");
                            }
                            rsInsert.next();
                            id_ocorrencia = rsInsert.getLong("id");
                            rsInsert.close();

                            //InserÃ§Ã£o de RAS
                            insertRAS += "(\'" + cod_rgo + "\'";  //id
                            insertRAS += ", \' " + new java.sql.Date(data.getTime()) + "\'";
                            insertRAS += ", \'" + new java.sql.Time(horario.getTime()) + "\'";
                            insertRAS += ", \'" + tipoAcidente + "\'"; // TipoAcidente
                            insertRAS += ", \'" + end.getBairro() + "\'"; // bairro
                            insertRAS += ", \'" + end.getCep() + "\'";   // cep
                            insertRAS += ", \'" + end.getCidade() + "\'";  // cidade
                            if (end.getCoordenadas() != null) {
                                insertRAS += ", " + "ST_GeomFromText('" + end.getCoordenadas() + "',4326)";  // coordenadas
                            }
                            insertRAS += ", \' " + end.getNumero() + "\'";  // numero
                            insertRAS += ", \'" + end.getRua() + "\'";  // rua
                            insertRAS += ", \'" + end.getCruzamento() + "\'";  // cruzamento (boolean)
                            insertRAS += ", \'" + end.getCruzamentoCom() + "\'";  // cruzamentoCom (String)
                            insertRAS += ", \'SISTEMA\'";  // Usuario criador (criado pelo sistema)
                            insertRAS += ", \'" + new java.sql.Date(new Date().getTime()) + "\'";  // Data de criacao...
                            insertRAS += ", \'" + id_ocorrencia + "\') returning id;";  // Id da ocorrencia...

                            ResultSet rs2 = null;
                            try {
                                rs2 = Atualiza.Consulta_sigetrans(insertRAS);
                            } catch (Exception ex) {
                                System.out.println("Erro no ResultSet rs2");
                            }
                            rs2.next();
                            id_ras = rs2.getLong("id");
                            rs2.close();

                            envolvidoAtivo = verificaEnvolvidoAtivo(data);
                            System.gc();

                        }
//           Inserido ocorrencia e RAS... agora a parte de vitima, pessoa, envolvido e historico

                        /*A SQL ficou grande para reduzir a quantidade de informaÃ§Ã£o trafegando na rede, desta forma, Ã© carregado somente
                         os campos necessÃ¡rios ao sigetrans.*/
                        String queryVitimaSQL = "select vitima as nome_vitima,situa_vitima, idade, sexo, tab_lesao.tipo as lesao, tab_posicao.nome as posicao_veiculo,"
                                + " tab_seguranca.nome as condicao_seguranca, tab_ocular.nome as abertura_ocular, pressaoarterial,"
                                + " tab_verbal.nome as resposta_verbal, tab_motora.nome as resposta_motora, recusa, pulso, sao2,"
                                + " freqrespiracao, array_sinaisclinicos, sinaisclinicos, tab_encaminhamento.nome as encaminhamento, localdestino,"
                                + " respvitima, medicosolicitado, medicocomparece, medicointerveio, medicohospital, escalatrauma, escalaglasgow,"
                                + " cranio, face, pescoco, dorso, torax, abdomen, pelvis, msd, mse, mid, mie, array_procedimentos"
                                + " from (select fk_rgo, vitima, idade, sexo, cod_lesao, recusa, situa_vitima, posicao, seguranca, aberturaocular, respverbal,"
                                + " respostamotora, pulso, sao2, pressaoarterial, freqrespiracao, array_sinaisclinicos, cranio, face, pescoco, dorso, torax,"
                                + " abdomen, pelvis, msd, mse, mid, mie, array_procedimentos, sinaisclinicos, destino, localdestino, respvitima, medicosolicitado,"
                                + " medicocomparece, medicointerveio, medicohospital, escalatrauma, escalaglasgow from rgo_vitima where fk_rgo=" + result[1] + ")as tab_vitima"
                                + " inner join (select cod_encaminhamento, nome from ts_encaminhamento) as tab_encaminhamento on(cod_encaminhamento=destino)"
                                + " inner join (select cod_lesao, tipo from ts_lesao) as tab_lesao using(cod_lesao)"
                                + " inner join (select cod_posicionavit, nome from ts_posicionavit) as tab_posicao on(cod_posicionavit=posicao)"
                                + " inner join (select cod_segurancavit, nome from ts_segurancavit) as tab_seguranca on(cod_segurancavit=seguranca)"
                                + " inner join (select cod_ocular, nome from ts_ocularvit) as tab_ocular on(cod_ocular=aberturaocular)"
                                + " inner join (select cod_respverbal, nome from ts_respverbal) as tab_verbal on(cod_respverbal=respverbal)"
                                + " inner join (select cod_respmotora, nome from ts_respmotora) as tab_motora on(cod_respmotora=respostamotora)";

                        /* Consultando sobre os veiculos envolvidos. Mais abaixo, sera usado para o cadastro de veiculos e envolvidos*/
                        ResultSet veiculos = Atualiza.Consulta_bombeiros("select id_rgo_veiculo,placa, municipio, fk_tipotransporte, nome as tpveiculo, condutor from rgo_veiculos "
                                + "inner join ts_tipotransporte on (cod_tptransporte = fk_tipotransporte)"
                                + " where fk_rgo=\'" + result[1] + "\'");

                        List<TempVeiculo> listVeiculos = new ArrayList<>();
                        List<TempVeiculo> listVeiculos_aux = new ArrayList<>();
                        while (veiculos.next()) {
                            listVeiculos.add(new TempVeiculo(veiculos.getLong("id_rgo_veiculo"), veiculos.getString("placa"), veiculos.getString("municipio"),
                                    veiculos.getString("condutor"), veiculos.getString("tpveiculo")));
                            listVeiculos_aux.add(new TempVeiculo(veiculos.getLong("id_rgo_veiculo"), veiculos.getString("placa"), veiculos.getString("municipio"),
                                    veiculos.getString("condutor"), veiculos.getString("tpveiculo")));

                        }

                        try (ResultSet resultVitima = Atualiza.Consulta_bombeiros(queryVitimaSQL)) {
                            int countVitima = 0;
                            while (resultVitima.next()) {
                                System.out.println("Buscando vitimas...");
                                countVitima++;

                                EnumSexo sexo;
                                if (resultVitima.getString("sexo").equalsIgnoreCase("m")) {
                                    sexo = EnumSexo.MASCULINO;
                                } else {
                                    if (resultVitima.getString("sexo").equalsIgnoreCase("f")) {
                                        sexo = EnumSexo.FEMININO;
                                    } else {
                                        sexo = EnumSexo.NAO_INFORMADO;
                                    }
                                }

                                EnumPosicaoNoVeiculo enumPosicaoNoVeiculo;
                                switch (resultVitima.getString("posicao_veiculo")) {
                                    case "Condutor":
                                        enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.CONDUTOR;
                                        break;
                                    case "Banco dianteiro":
                                        enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.BANCO_DIANTEIRO;
                                        break;
                                    case "Banco traseiro":
                                        enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.BANCO_TRASEIRO;
                                        break;
                                    case "Garupa":
                                        enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.GARUPA;
                                        break;
                                    default:
                                        enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.NAO_INFORMADO;
                                        break;
                                }

                                EnumCondicaoSeguranca enumCondicaoSeguranca;
                                switch (resultVitima.getString("condicao_seguranca")) {
                                    case "Usava Cinto de segurança":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.USAVA_CINTO;
                                        break;
                                    case "Usava capacete":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.USAVA_CAPACETE;
                                        break;
                                    case "Sem capacete":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.SEM_CAPACETE;
                                        break;
                                    case "Sem cinto de segurança":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.SEM_CINTO;
                                        break;
                                    case "Não é o caso":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.NAO_EXIGIVEL;
                                        break;
                                    case "Bebê-conforto":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.CADEIRINHA;
                                        break;
                                    case "Cadeirinha de segurança":
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.CADEIRINHA;
                                        break;
                                    default:
                                        enumCondicaoSeguranca = EnumCondicaoSeguranca.NAO_OBSERVADO;
                                        break;
                                }

                                EnumAberturaOcular enumAberturaOcular = null;
                                switch (resultVitima.getString("abertura_ocular")) {
                                    case "Ausente":
                                        enumAberturaOcular = EnumAberturaOcular.AUSENTE;
                                        break;
                                    case "À dor":
                                        enumAberturaOcular = EnumAberturaOcular.A_DOR;
                                        break;
                                    case "À voz":
                                        enumAberturaOcular = EnumAberturaOcular.A_VOZ;
                                        break;
                                    case "Espontânea":
                                        enumAberturaOcular = EnumAberturaOcular.ESPONTANEA;
                                        break;
                                }

                                EnumRespostaVerbal enumRespostaVerbal = null;
                                switch (resultVitima.getString("resposta_verbal")) {
                                    case "Ausente":
                                        enumRespostaVerbal = EnumRespostaVerbal.AUSENTE;
                                        break;
                                    case "Incompreensível":
                                        enumRespostaVerbal = EnumRespostaVerbal.INCOMPREENSIVEL;
                                        break;
                                    case "Desconexo":
                                        enumRespostaVerbal = EnumRespostaVerbal.DESCONEXO;
                                        break;
                                    case "Confuso":
                                        enumRespostaVerbal = EnumRespostaVerbal.CONFUSO;
                                        break;
                                    case "Orientado":
                                        enumRespostaVerbal = EnumRespostaVerbal.ORIENTADO;
                                        break;
                                }

                                EnumRespostaMotora enumRespostaMotora = null;
                                String result1 = resultVitima.getString("resposta_motora");
                                if (result1.matches("Au.*")) {
                                    enumRespostaMotora = EnumRespostaMotora.AUSENTE;
                                } else if (result1.matches("Ex.*")) {
                                    enumRespostaMotora = EnumRespostaMotora.EXTENSAO;
                                } else if (result1.matches("F.*")) {
                                    enumRespostaMotora = EnumRespostaMotora.FLEXAO;
                                } else if (result1.matches("Ret.*")) {
                                    enumRespostaMotora = EnumRespostaMotora.RETIRADA_A_DOR;
                                } else if (result1.matches("Ap.*")) {
                                    enumRespostaMotora = EnumRespostaMotora.APROPRIADA_A_DOR;
                                } else if (result1.matches("Ob.*")) {
                                    enumRespostaMotora = EnumRespostaMotora.RESPONDE_COMANDOS;
                                }

                                String destinoVitima = "";
                                String localDestino = resultVitima.getString("localdestino").replaceAll("['<>\\|/]", " ");
                                localDestino = localDestino.toUpperCase();  //Converte a string toda para maiusculo

                                boolean insertHospital = false;

                                // Aqui onde o bixo pega, nÃ£o existe padrÃ£o no campo e nem na informaÃ§Ã£o...
                                //EntÃ£o o jeito Ã© pegar o que conhecemos... :(
                                if (localDestino.matches("LIBERAD.*")) {
                                    //Nesse caso o encaminhamento foi listado errado no formulÃ¡rios dos bombeiros
                                    destinoVitima = "Liberado no local";

                                } else {
                                    if (localDestino.contains("RECU") || localDestino.contains("RESCU")) {
                                        //Nesse caso o encaminhamento foi listado errado no formulÃ¡rios dos bombeiros
                                        if (localDestino.contains("ENCAM")) {
                                            destinoVitima = "Recusou encaminhamento hospital";

                                        } else {
                                            destinoVitima = "Recusou atendimento";

                                        }
                                    } else {
                                        /*
                                         * HU
                                         * H U
                                         * Hospital UniversitÃ¡rio
                                         */
                                        Pattern patternHospital = Pattern.compile("[H][A-Z]*\\s?[U][A-Z]*|[H][A-Z]*\\s*[R][A-Z]* | UNIVER[A-Z]*");
                                        if (patternHospital.matcher(localDestino).find()) {
                                            localDestino = "HOSPITAL UNIVERSITÁRIO DO OESTE DO PARANÁ";
                                            destinoVitima = "Entregue no hospital";
                                        } else {
                                            /*
                                             * UPA
                                             * U P A
                                             * Unidade de pronto atendimento
                                             */
                                            patternHospital = Pattern.compile("[U][A-Z]*\\s*[P][A-Z0-9]* | PAC");
                                            if (patternHospital.matcher(localDestino).find()) {
                                                if (localDestino.contains("3") || localDestino.contains("III") || localDestino.contains("LLL") || localDestino.toUpperCase().contains("VENE")) {
                                                    localDestino = "UPA 3";
                                                    destinoVitima = "Entregue no hospital";
                                                } else {
                                                    if (localDestino.contains("2") || localDestino.contains("II") || localDestino.contains("LL") || localDestino.toUpperCase().contains("BRAS")) {
                                                        localDestino = "UPA 2";
                                                        destinoVitima = "Entregue no hospital";
                                                    } else {
                                                        if (localDestino.contains("1") || localDestino.contains("I") || localDestino.contains("L") || localDestino.toUpperCase().contains("PEDIA")) {
                                                            localDestino = "UPA 1";
                                                            destinoVitima = "Entregue no hospital";
                                                        } else {
                                                            localDestino = "NÃO INFORMADO";
                                                            destinoVitima = "Não informado";
                                                        }
                                                    }
                                                }
                                                //Finalizado os tratamentos de UPA
                                            } else {
                                                /*
                                                 * SÃ£o Lucas
                                                 * Hospital SÃ£o Lucas
                                                 */
                                                if (localDestino.contains("LUCAS")) {
                                                    localDestino = "HOSPITAL SÃO LUCAS";
                                                    destinoVitima = "Entregue no hospital";
                                                } else {
                                                    if (localDestino.contains("CATARINA") || localDestino.contains("STA")) {
                                                        localDestino = "HOSPITAL E MATERNIDADE SANTA CATARINA";
                                                        destinoVitima = "Entregue no hospital";
                                                    } else {
                                                        if (localDestino.contains("SALETE")) {
                                                            localDestino = "HOSPITAL NOSSA SENHORA DA SALETE";
                                                            destinoVitima = "Entregue no hospital";
                                                        } else {
                                                            if (localDestino.contains("POLI")) {
                                                                localDestino = "HOSPITAL POLICLÍNICA DE CASCAVEL";
                                                                destinoVitima = "Entregue no hospital";
                                                            } else {
                                                                if (localDestino.contains("LIMA")) {
                                                                    localDestino = "HOSPITAL E MATERNIDADE DOUTOR LIMA";
                                                                    destinoVitima = "Entregue no hospital";
                                                                } else {
                                                                    if (localDestino.contains("IML") || localDestino.contains("LEGAL")) {
                                                                        localDestino = "IML";
                                                                        destinoVitima = "Entregue no hospital";
                                                                    } else {
                                                                        localDestino = "NÃO INFORMADO";
                                                                        destinoVitima = "Não informado";
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                String tipoVeiculo = "Não informado";
                                /*Se o envolvido for condutor, informar o tipo de veiculo*/

                                for (TempVeiculo veiculo : listVeiculos_aux) {
                                    //tenso que os caras erram o nome aqui rsrs...
                                    //escrevem de um jeito na RAS e no cadastro de veiculo de outro Â¬Â¬
                                    try {
                                        String[] nome = resultVitima.getString("nome_vitima").split(" ");
                                        String nome1 = nome[0] + " " + nome[1];

                                        if (veiculo.getCondutor().equalsIgnoreCase(nome1)) {
                                            tipoVeiculo = veiculo.getTipoVeiculo();
                                            break;
                                        }
                                    } catch (Exception ex) {
                                        if (veiculo.getCondutor().equalsIgnoreCase(resultVitima.getString("nome_vitima"))) {
                                            tipoVeiculo = veiculo.getTipoVeiculo();
                                            break;

                                        }
                                    }
                                }

                                long situa = resultVitima.getInt("situa_vitima");
                                for (TempVeiculo veiculo : listVeiculos_aux) {
                                    if (situa == veiculo.getRgo_veiculo()) {
                                        tipoVeiculo = veiculo.getTipoVeiculo();
                                    }
                                }

                                String tiposituacao = "";
                                if (situa == 0) {
                                    tiposituacao = "A pé";
                                } else {
                                    if (tipoVeiculo.matches("Auto.*")) {
                                        tiposituacao = "Em auto";
                                    } else {
                                        if (tipoVeiculo.matches("Bici.*")) {
                                            tiposituacao = "Em bicicleta";
                                        } else {
                                            if (tipoVeiculo.matches("Mot.*")) {
                                                tiposituacao = "Em moto";
                                            } else {
                                                if (tipoVeiculo.matches("On.*")) {
                                                    tiposituacao = "Em ônibus";
                                                } else {
                                                    tiposituacao = "Não informado";
                                                }
                                            }

                                        }
                                    }
                                }
                                String insertVitima = preparaConsultaVitimaRAS();

                                //InserÃ§Ã£o da vÃ­tima
                                insertVitima += "(\'" + enumAberturaOcular + "\'";
                                insertVitima += ", " + (resultVitima.getString("medicohospital").equalsIgnoreCase("sim") ? true : false);
                                insertVitima += ", " + (resultVitima.getString("medicocomparece").equalsIgnoreCase("sim") ? true : false);
                                insertVitima += ", " + (resultVitima.getString("medicointerveio").equalsIgnoreCase("sim") ? true : false);
                                insertVitima += ", " + (resultVitima.getString("medicosolicitado").equalsIgnoreCase("sim") ? true : false);
                                insertVitima += ", \'" + new java.sql.Date(new Date().getTime()) + "\'"; //Data de criacao
                                insertVitima += ", \'\'";  //endereÃ§o vitima
                                insertVitima += ", \'" + EnumEscalaComa._0 + "\'";
                                insertVitima += ", \'" + Integer.parseInt(resultVitima.getString("escalaglasgow")) + "\'";
                                insertVitima += ", \'\'";
                                insertVitima += ", \'" + resultVitima.getString("escalatrauma") + "\'";
                                insertVitima += ", \'" + resultVitima.getString("freqrespiracao").replaceAll("['<>\\|/]", " ") + "\'";
                                insertVitima += ", \'" + Integer.parseInt(resultVitima.getString("idade")) + "\'";
                                insertVitima += ", \'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\'";
                                insertVitima += ", " + countVitima;
                                insertVitima += ", \'" + resultVitima.getString("lesao") + "\'";
                                insertVitima += ", \'" + resultVitima.getString("pressaoarterial") + "\'";
                                insertVitima += ", \'" + EnumPAMaxima._0 + "\'";
                                insertVitima += ", \'" + resultVitima.getString("pulso").replaceAll("['<>\\|/]", " ") + "\'";
                                insertVitima += ", \'" + EnumRespiracaoMinuto._0 + "\'";
                                insertVitima += ", \'" + enumRespostaMotora + "\'";
                                insertVitima += ", \'" + enumRespostaVerbal + "\'";
                                insertVitima += ", \'" + resultVitima.getString("sao2").replaceAll("['<>\\|/]", " ") + "\'";
                                insertVitima += ", \'" + sexo + "\'";
                                insertVitima += ", \'" + resultVitima.getString("respvitima").replaceAll("['<>\\|/]", " ") + "\'";
                                insertVitima += ", \'" + enumCondicaoSeguranca + "\'";
                                insertVitima += ", \'" + enumPosicaoNoVeiculo + "\'";
                                insertVitima += ", " + false + "";
                                insertVitima += ", \'SISTEMA\'"; // Usuario criador -> SISTEMA 
                                insertVitima += ", \'" + destinoVitima + "\'";
                                insertVitima += ", \'" + localDestino + "\'";
                                insertVitima += ", \'" + id_ras + "\'";
                                insertVitima += ", \'" + tiposituacao + "\'";
                                insertVitima += ", \'" + tipoVeiculo + "\'";

                                //Pegando Sinais clinicos...
                                String sinaisClinicos = resultVitima.getString("array_sinaisclinicos");
                                if (!sinaisClinicos.equals("")) {
                                    String[] arraySinaisClinicos = sinaisClinicos.split(",");
                                    for (int i = 0; i < arraySinaisClinicos.length; i++) {
//                            System.out.println("INSERT INTO tiposinalclinico (descricao) VALUES(\'" + arraySinaisClinicos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                           Atualiza.executeSQL_sigetrans("INSERT INTO tiposinalclinico (descricao) VALUES(\'" + arraySinaisClinicos[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }

                                insertVitima += ", \'" + sinaisClinicos.replaceAll("['<>\\|/]", " ") + "\'";

                                //Pegando procedimentos...
                                String procedimentos = resultVitima.getString("array_procedimentos");
                                if (!procedimentos.equals("")) {
                                    String[] arrayProcedimentos = procedimentos.split(", ");
                                    for (int i = 0; i < arrayProcedimentos.length; i++) {
//                            System.out.println("INSERT INTO tipoprocedimento (descricao) VALUES(\'" + arrayProcedimentos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO tipoprocedimento (descricao) VALUES(\'" + arrayProcedimentos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                            vitimaRAS.getProcedimentos().add(new TipoProcedimento(arrayProcedimentos[i]));                      
                                    }
                                }
                                insertVitima += ", \'" + procedimentos.replaceAll("['<>\\|/]", " ") + "\'";

                                String lesao = resultVitima.getString("cranio");
                                String[] vet = lesao.split(",");

                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }

                                //cranio
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("face");
                                vet = lesao.split(", ");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }

                                //face
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("pescoco");
                                vet = lesao.split(", ");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");

                                    }
                                }
                                //Pescoco
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("dorso");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");

                                    }
                                }
                                //dorso
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("torax");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }
                                //torax
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("abdomen");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }
                                //abdomen
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("pelvis");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }
                                //pelvis
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("msd");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }
                                //msd
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("mse");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }

                                //mse
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("mie");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                            Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }

                                //mie
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";

                                lesao = resultVitima.getString("mid");
                                vet = lesao.split(",");
                                if (vet.length > 0 && !vet[0].equals("")) {
                                    for (int i = 0; i < vet.length; i++) {
                                        if (vet[i].startsWith(" ")) {
                                            vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                        }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                    }
                                }
                                //mid
                                insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\') returning id;";

//                    System.out.println(updateVitima);
                                ResultSet rsInsert2 = Atualiza.Consulta_sigetrans(insertVitima);

                                rsInsert2.next();
                                Long id_vitimaRAS = rsInsert2.getLong("id");
                                rsInsert2.close();

                                /*Verifica (usando o nome) se a pessoa jÃ¡ estÃ¡ cadastrada. Caso esteja, um novo registro vinculado Ã© criado */
//                    System.out.println("SELECT id, nome from pessoa where nome=\'" + vitimaRAS.getNomeVitima().replaceAll("['<>\\|/]", " ") + "\'");
                                ResultSet pessoasResult = Atualiza.Consulta_sigetrans("SELECT id, nome from pessoa where nome=\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\'");
                                Long id_pessoa;

                                if (pessoasResult.next()) {
                                    id_pessoa = pessoasResult.getLong("id");
                                } else {
                                    //NÃ£o encontrou pessoa semelhante na base de dados
                                    String insertPessoa = preparaConsultaPessoa();

                                    insertPessoa += "(\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/.]", " ") + "\'";
                                    insertPessoa += ", \'\') returning id;";

//                        System.out.println(insertPessoa);
                                    ResultSet pessoaInserted = Atualiza.Consulta_sigetrans(insertPessoa);
                                    pessoaInserted.next();
                                    id_pessoa = pessoaInserted.getLong("id");
                                }

                                Long id_condutor = null;

                                for (TempVeiculo veiculo : listVeiculos) {
                                    if (veiculo.getCondutor().equals(resultVitima.getString("nome_vitima"))) {
                                        ResultSet resultV = Atualiza.Consulta_sigetrans("INSERT INTO veiculo(cidade, placa) VALUES (\'" + veiculo.getMunicipio().replaceAll("['<>\\|/]", " ") + "\', \'" + veiculo.getPlaca().replaceAll("['<>\\|/]", " ") + "\') returning id;");
                                        resultV.next();
                                        Long id_veiculo = resultV.getLong("id");

                                        ResultSet resultCondutor = Atualiza.Consulta_sigetrans("INSERT INTO condutor(veiculo_id, sintomasembriaguez, solicitacaoexame, estadocondutor, categoriahabilitacao, situacaohabilitacao, tipohabilitacao) VALUES (" + id_veiculo + ", false, false, 'NAO_INFORMADO', 'NAO_INFORMADO', 'NAO_INFORMADO', 'NAO_INFORMADO') returning id;");
                                        resultCondutor.next();
                                        id_condutor = resultCondutor.getLong("id");
                                        listVeiculos.remove(veiculo);
                                        break;
                                    }
                                }

                                String insertEnvolvido = preparaConsultaEnvolvido();

                                insertEnvolvido += "(\'" + envolvidoAtivo + "\'";
                                insertEnvolvido += ", \'" + EnumCondicaoSeguranca.NAO_OBSERVADO + "\'";
                                insertEnvolvido += ", \'" + new java.sql.Date(new Date().getTime()) + "\'";
                                insertEnvolvido += ", \'" + Integer.parseInt(resultVitima.getString("idade")) + "\'";
                                insertEnvolvido += ", \'" + enumPosicaoNoVeiculo + "\'";
                                insertEnvolvido += ", \'vitima\'";
                                insertEnvolvido += ", \'SISTEMA\'";
                                insertEnvolvido += ", \'" + id_pessoa + "\'";
                                insertEnvolvido += ", \'" + id_ocorrencia + "\'";
                                insertEnvolvido += ", \'Não informado\'";
                                insertEnvolvido += ", \'" + tipoVeiculo + "\'";
                                insertEnvolvido += ", \'" + new TipoProfissao().getDescricao() + "\'";
                                insertEnvolvido += ", \'" + EnumEscolaridade.NAO_INFORMADO + "\'";
                                insertEnvolvido += ", \'" + EnumSituacaoCivil.NAO_INFORMADO + "\'";
                                insertEnvolvido += ", \'" + false + "\'";
                                insertEnvolvido += ", " + id_condutor;
                                insertEnvolvido += ", \'" + id_vitimaRAS + "\') returning id;";

//                    System.out.println(UpdateEnvolvido);
                                Atualiza.executeSQL_sigetrans(insertEnvolvido);

                            }
                        }
                        for (TempVeiculo veiculo : listVeiculos) {
                            /* Este caso trata-se de um envolvido que nÃ£o o teve atendimento mÃ©dico - logo, nÃ£o teve RAS. 
                             
                             * Primeiro passo: Inserir uma nova pessoa
                             * Segundo passo: Inserir tipo de veiculo
                             * Terceiro passo: Inserir novo envolvido
                             * Quarto passo: Inserir veiculo/condutor
                             */
                            query = "SELECT id, nome from pessoa where nome=\'" + veiculo.getCondutor().replaceAll("['<>\\|/]", " ") + "\'";
                            ResultSet pessoasResult = Atualiza.Consulta_sigetrans(query);
                            Long id_pessoa;
                            if (pessoasResult.next()) {
                                id_pessoa = pessoasResult.getLong("id");
                            } else {
                                //NÃ£o encontrou pessoa semelhante na base de dados
                                String insertPessoa = preparaConsultaPessoa();

                                insertPessoa += "(\'" + veiculo.getCondutor().replaceAll("['<>\\|/]", " ") + "\'";
                                insertPessoa += ", \'\') returning id;";

//                    System.out.println(insertPessoa);
                                ResultSet pessoaInserted = Atualiza.Consulta_sigetrans(insertPessoa);
                                pessoaInserted.next();
                                id_pessoa = pessoaInserted.getLong("id");
                            }

//                System.out.println("INSERT INTO tipoveiculo (descricao) VALUES(\'" + veiculo.getTipoVeiculo() + "\');");
//                LocalCon.executeSQL("INSERT INTO tipoveiculo (descricao) VALUES(\'" + veiculo.getTipoVeiculo() + "\');");
                            String insertEnvolvido = "INSERT INTO envolvido("
                                    + "ativo,"
                                    + "condicaoseguranca,"
                                    + "datacriacao,"
                                    + "posicaonoveiculo,"
                                    + "tiporegistro,"
                                    + "usuario_login,"
                                    + "pessoa_id,"
                                    + "ocorrencia_id, "
                                    + "situacao, "
                                    + "profissao, "
                                    + "escolaridade, "
                                    + "estadocivil, "
                                    + "acidentedetrabalho, "
                                    + "tipoveiculo)"
                                    + " VALUES ";

                            insertEnvolvido += "(\'" + false + "\'";
                            insertEnvolvido += ", \'" + EnumCondicaoSeguranca.NAO_OBSERVADO + "\'";
                            insertEnvolvido += ", \'" + new java.sql.Date(new Date().getTime()) + "\'";
                            insertEnvolvido += ", \'" + EnumPosicaoNoVeiculo.CONDUTOR + "\'";
                            insertEnvolvido += ", \'envolvido\'";
                            insertEnvolvido += ", \'SISTEMA\'";
                            insertEnvolvido += ", \'" + id_pessoa + "\'";
                            insertEnvolvido += ", \'" + id_ocorrencia + "\'";
                            insertEnvolvido += ", \'Não informado\'";
                            insertEnvolvido += ", \'" + new TipoProfissao().getDescricao() + "\'";
                            insertEnvolvido += ", \'" + EnumEscolaridade.NAO_INFORMADO + "\'";
                            insertEnvolvido += ", \'" + EnumSituacaoCivil.NAO_INFORMADO + "\'";
                            insertEnvolvido += ", \'" + false + "\'";
                            insertEnvolvido += ", \'" + veiculo.getTipoVeiculo() + "\') returning id;";

//                System.out.println(insertEnvolvido);
                            ResultSet envolvidoInserted = Atualiza.Consulta_sigetrans(insertEnvolvido);
                            envolvidoInserted.next();
                            Long id_envolvido = envolvidoInserted.getLong("id");

                            query = "INSERT INTO veiculo(cidade, placa) VALUES (\'" + veiculo.getMunicipio().replaceAll("['<>\\|/]", " ") + "\', \'" + veiculo.getPlaca().replaceAll("['<>\\|/]", " ") + "\') returning id;";
//                System.out.println(query);
                            ResultSet resultV = Atualiza.Consulta_sigetrans(query);
                            resultV.next();
                            Long id_veiculo = resultV.getLong("id");
                            query = "INSERT INTO condutor(veiculo_id, sintomasembriaguez, solicitacaoexame, estadocondutor, categoriahabilitacao, situacaohabilitacao, tipohabilitacao) VALUES (\'" + id_veiculo + "\', false, false, \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\') returning id;";
//                System.out.println(query);
                            Atualiza.executeSQL_sigetrans(query);
                        }
                        System.gc();
                    } else {
                        id_ras = ras.getLong("id");
                        id_ocorrencia = ras.getLong("ocorrencia_id");
                        System.out.println("Vítima já registrada!");
                    }
                } catch (PSQLException p) {
                    p.printStackTrace();
                    System.exit(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(11);
                }
            } else {
                //************************UPDATE***************************
                if (bombeiros.getOper().equals("u")) {
                    try {
                        System.out.println("UPDATE - id " + bombeiros.getId());

                        //Para se fazer update é necessario que o caso já esteja no banco.
                        if (ras.next() && rs.next()) {

                            System.out.println("\nCod RGO: " + rs.getString("cod_rgo"));

                            String UpdateOcorrencia = "UPDATE public.ocorrencia SET";
                            String UpdateRAS = "UPDATE formularios.ras SET";

                            String cod_rgo = rs.getString("cod_rgo");
                            Date data = processDate(rs.getString("datahora"), true);
                            Date horario = processDate(rs.getString("datahora"), false);


                            /*Verifica a rua e tenta colocar no padrÃ£o google maps do SIGETRANS
                             Se o endereÃ§o nÃ£o contiver um nÃºmero aproximado, a consulta ao google maps serÃ¡ ignorada
                             e o endereÃ§o obtido na base dos bombeiros sera usado */
                            Endereco end = null;
                            if (isDigit(rs.getString("numero")) && Integer.parseInt(rs.getString("numero")) != 0) {
                                end = processaEndereco(rs.getString("rua"), rs.getString("numero"), rs.getString("cep"), rs.getString("bairro"), rs.getString("cidade"), rs.getString("cruzamento"));
                            } else {
                                end = processaEndereco(rs.getString("endereco_geral.log_nome"), rs.getString("cruzamento"), rs.getString("cep"), rs.getString("bairro"), rs.getString("cidade"));
                            }

                            /*Usar o replaceAll para campos que nÃ£o possuem valores prÃ©-definidos na base dos bombeiros
                             isso evita erros de inserÃ§Ã£o (Erros de SQL) por conter caracteres especiais*/
                            String observacoes = rs.getString("referencia").replaceAll("['<>\\|/]", " ");

                            String tipoAcidente = rs.getString("ocorrencia");

                            if (tipoAcidente.contains("-")) {
                                tipoAcidente = tipoAcidente.split("- ")[1];
                            }

                            String tipoCausa = rs.getString("causa").replaceAll("['<>\\|/]", " ");

                            //Verifica se existe o tipo no banco
                            ResultSet consulta = null;
                            try {
                                consulta = Atualiza.Consulta_sigetrans("SELECT descricao FROM tipocausa WHERE descricao=\'" + tipoCausa + "\'");
                            } catch (Exception ex) {
                                System.out.println("Erro Resultset consulta (update)");
                            }
                            if (consulta.wasNull()) {
                                tipoCausa = "Não apurado";
                            }

                            String severidade;
                            if (rs.getString("possui_vitima").equals("Sim")) {
                                severidade = "Com vítimas";
                            } else {
                                severidade = "Sem vítimas";
                            }

                            //InserÃ§Ã£o de RAS
                            UpdateRAS += " dataacidente=\' " + new java.sql.Date(data.getTime()) + "\'";
                            UpdateRAS += ", horarioacidente=\'" + new java.sql.Time(horario.getTime()) + "\'";
                            UpdateRAS += ", tipoacidente=\'" + tipoAcidente + "\'"; // TipoAcidente
                            UpdateRAS += ", bairro=\'" + end.getBairro() + "\'"; // bairro
                            UpdateRAS += ", cep=\'" + end.getCep() + "\'";   // cep
                            UpdateRAS += ", cidade=\'" + end.getCidade() + "\'";  // cidade
                            if (end.getCoordenadas() != null) {
                                UpdateRAS += ", coordenadas=ST_GeomFromText('" + end.getCoordenadas() + "',4326)";  // coordenadas
                            }
                            UpdateRAS += ", numero=\' " + end.getNumero() + "\'";  // numero
                            UpdateRAS += ", rua=\'" + end.getRua() + "\'";  // rua
                            UpdateRAS += ", cruzamento=\'" + end.getCruzamento() + "\'";  // cruzamento (boolean)
                            UpdateRAS += ", cruzamentocom=\'" + end.getCruzamentoCom() + "\'";  // cruzamentoCom (String)
                            UpdateRAS += ", datacriacao=\'" + new java.sql.Date(new Date().getTime()) + "\' where rgo=" + cod_rgo + " returning id,ocorrencia_id;";

                            ResultSet rs2 = null;
                            try {
                                rs2 = Atualiza.Consulta_sigetrans(UpdateRAS);
                            } catch (Exception ex) {
                                System.out.println("Erro no rs2 update");
                            }
                            rs2.next();
                            id_ocorrencia = rs2.getLong("ocorrencia_id");
                            id_ras = rs2.getLong("id");
                            rs2.close();

                            //InserÃ§Ã£o de ocorrencia
                            UpdateOcorrencia += " dataacidente=\'" + new java.sql.Date(data.getTime()) + "\'";
                            UpdateOcorrencia += ", horarioacidente=\'" + new java.sql.Time(horario.getTime()) + "\'";
                            UpdateOcorrencia += ", tipoacidente=\'" + tipoAcidente + "\'"; // TipoAcidente
                            UpdateOcorrencia += ", clima=\'Não informado\'"; // TipoClima
                            UpdateOcorrencia += ", conservacao=\'Não informado\'"; // TipoConservacao
                            UpdateOcorrencia += ", pavimentacao=\'Não informado\'"; // TipoPavimentacao
                            UpdateOcorrencia += ", perfilpista=\'Não informado\'"; // TipoPerfilPista
                            UpdateOcorrencia += ", provavelcausa=\'" + tipoCausa + "\'"; // TipoCausa  (tinha que tratar a causa...)
                            UpdateOcorrencia += ", severidade=\'" + severidade + "\'"; // TipoSeveridade (tinha que vericar se tinha vitimas ou nÃ£o)
                            UpdateOcorrencia += ", superficie=\'Não informado\'"; // TipoSuperficie
                            UpdateOcorrencia += ", bairro=\'" + end.getBairro().replaceAll("['<>\\|/]", " ") + "\'"; // bairro
                            UpdateOcorrencia += ", cep=\'" + end.getCep() + "\'";   // cep
                            UpdateOcorrencia += ", cidade=\'" + end.getCidade().replaceAll("['<>\\|/]", " ") + "\'";  // cidade
                            if (end.getCoordenadas() != null) {
                                UpdateOcorrencia += ", coordenadas=" + "ST_GeomFromText('" + end.getCoordenadas() + "',4326)";  // coordenadas
                            }
                            UpdateOcorrencia += ", numero=\' " + end.getNumero() + "\'";  // numero
                            UpdateOcorrencia += ", rua=\'" + end.getRua().replaceAll("['<>\\|/]", " ") + "\'";  // rua
                            UpdateOcorrencia += ", cruzamento=\'" + end.getCruzamento() + "\'";  // cruzamento (boolean)
                            UpdateOcorrencia += ", cruzamentocom=\'" + end.getCruzamentoCom().replaceAll("['<>\\|/]", " ") + "\'";  // cruzamentoCom (String)
                            UpdateOcorrencia += ", datacriacao=\'" + new java.sql.Date(new Date().getTime()) + "\'";  // Data de criacao...
                            UpdateOcorrencia += ", observacao=\'" + observacoes + "\'";  // Observacao...
                            UpdateOcorrencia += ", sinalizacao=\'Não informado\' where id=" + id_ocorrencia + " returning id;";  // sinalizacoes...

                            ResultSet Updateocorrencia = null;
                            try {
                                Updateocorrencia = Atualiza.Consulta_sigetrans(UpdateOcorrencia);
                            } catch (Exception ex) {
                                System.out.println("Erro update ocorrencia");
                            }

                            envolvidoAtivo = verificaEnvolvidoAtivo(data);
                            System.gc();


                            /*A SQL ficou grande para reduzir a quantidade de informaÃ§Ã£o trafegando na rede, desta forma, Ã© carregado somente
                             os campos necessÃ¡rios ao sigetrans.*/
                            String queryVitimaSQL = "select vitima as nome_vitima,situa_vitima, idade, sexo, tab_lesao.tipo as lesao, tab_posicao.nome as posicao_veiculo,"
                                    + " tab_seguranca.nome as condicao_seguranca, tab_ocular.nome as abertura_ocular, pressaoarterial,"
                                    + " tab_verbal.nome as resposta_verbal, tab_motora.nome as resposta_motora, recusa, pulso, sao2,"
                                    + " freqrespiracao, array_sinaisclinicos, sinaisclinicos, tab_encaminhamento.nome as encaminhamento, localdestino,"
                                    + " respvitima, medicosolicitado, medicocomparece, medicointerveio, medicohospital, escalatrauma, escalaglasgow,"
                                    + " cranio, face, pescoco, dorso, torax, abdomen, pelvis, msd, mse, mid, mie, array_procedimentos"
                                    + " from (select fk_rgo, vitima, idade, sexo, cod_lesao, recusa, situa_vitima, posicao, seguranca, aberturaocular, respverbal,"
                                    + " respostamotora, pulso, sao2, pressaoarterial, freqrespiracao, array_sinaisclinicos, cranio, face, pescoco, dorso, torax,"
                                    + " abdomen, pelvis, msd, mse, mid, mie, array_procedimentos, sinaisclinicos, destino, localdestino, respvitima, medicosolicitado,"
                                    + " medicocomparece, medicointerveio, medicohospital, escalatrauma, escalaglasgow from rgo_vitima where fk_rgo=" + result[1] + ")as tab_vitima"
                                    + " inner join (select cod_encaminhamento, nome from ts_encaminhamento) as tab_encaminhamento on(cod_encaminhamento=destino)"
                                    + " inner join (select cod_lesao, tipo from ts_lesao) as tab_lesao using(cod_lesao)"
                                    + " inner join (select cod_posicionavit, nome from ts_posicionavit) as tab_posicao on(cod_posicionavit=posicao)"
                                    + " inner join (select cod_segurancavit, nome from ts_segurancavit) as tab_seguranca on(cod_segurancavit=seguranca)"
                                    + " inner join (select cod_ocular, nome from ts_ocularvit) as tab_ocular on(cod_ocular=aberturaocular)"
                                    + " inner join (select cod_respverbal, nome from ts_respverbal) as tab_verbal on(cod_respverbal=respverbal)"
                                    + " inner join (select cod_respmotora, nome from ts_respmotora) as tab_motora on(cod_respmotora=respostamotora)";

                            /* Consultando sobre os veiculos envolvidos. Mais abaixo, sera usado para o cadastro de veiculos e envolvidos*/
                            ResultSet veiculos = Atualiza.Consulta_bombeiros("select id_rgo_veiculo,placa, municipio, fk_tipotransporte, nome as tpveiculo, condutor from rgo_veiculos "
                                    + "inner join ts_tipotransporte on (cod_tptransporte = fk_tipotransporte)"
                                    + " where fk_rgo=\'" + result[1] + "\'");

                            List<TempVeiculo> listVeiculos = new ArrayList<>();
                            List<TempVeiculo> listVeiculos_aux = new ArrayList<>();
                            while (veiculos.next()) {
                                listVeiculos.add(new TempVeiculo(veiculos.getLong("id_rgo_veiculo"), veiculos.getString("placa"), veiculos.getString("municipio"),
                                        veiculos.getString("condutor"), veiculos.getString("tpveiculo")));
                                listVeiculos_aux.add(new TempVeiculo(veiculos.getLong("id_rgo_veiculo"), veiculos.getString("placa"), veiculos.getString("municipio"),
                                        veiculos.getString("condutor"), veiculos.getString("tpveiculo")));

                            }

                            try (ResultSet resultVitima = Atualiza.Consulta_bombeiros(queryVitimaSQL)) {
                                int countVitima = 0;
                                System.out.println("Buscando vitimas...");
                                while (resultVitima.next()) {

                                    countVitima++;

                                    EnumSexo sexo;
                                    if (resultVitima.getString("sexo").equalsIgnoreCase("m")) {
                                        sexo = EnumSexo.MASCULINO;
                                    } else {
                                        if (resultVitima.getString("sexo").equalsIgnoreCase("f")) {
                                            sexo = EnumSexo.FEMININO;
                                        } else {
                                            sexo = EnumSexo.NAO_INFORMADO;
                                        }
                                    }

                                    EnumPosicaoNoVeiculo enumPosicaoNoVeiculo;
                                    switch (resultVitima.getString("posicao_veiculo")) {
                                        case "Condutor":
                                            enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.CONDUTOR;
                                            break;
                                        case "Banco dianteiro":
                                            enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.BANCO_DIANTEIRO;
                                            break;
                                        case "Banco traseiro":
                                            enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.BANCO_TRASEIRO;
                                            break;
                                        case "Garupa":
                                            enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.GARUPA;
                                            break;
                                        default:
                                            enumPosicaoNoVeiculo = EnumPosicaoNoVeiculo.NAO_INFORMADO;
                                            break;
                                    }

                                    EnumCondicaoSeguranca enumCondicaoSeguranca;
                                    switch (resultVitima.getString("condicao_seguranca")) {
                                        case "Usava Cinto de segurança":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.USAVA_CINTO;
                                            break;
                                        case "Usava capacete":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.USAVA_CAPACETE;
                                            break;
                                        case "Sem capacete":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.SEM_CAPACETE;
                                            break;
                                        case "Sem cinto de segurança":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.SEM_CINTO;
                                            break;
                                        case "Não é o caso":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.NAO_EXIGIVEL;
                                            break;
                                        case "Bebê-conforto":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.CADEIRINHA;
                                            break;
                                        case "Cadeirinha de segurança":
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.CADEIRINHA;
                                            break;
                                        default:
                                            enumCondicaoSeguranca = EnumCondicaoSeguranca.NAO_OBSERVADO;
                                            break;
                                    }

                                    EnumAberturaOcular enumAberturaOcular = null;
                                    switch (resultVitima.getString("abertura_ocular")) {
                                        case "Ausente":
                                            enumAberturaOcular = EnumAberturaOcular.AUSENTE;
                                            break;
                                        case "À dor":
                                            enumAberturaOcular = EnumAberturaOcular.A_DOR;
                                            break;
                                        case "À voz":
                                            enumAberturaOcular = EnumAberturaOcular.A_VOZ;
                                            break;
                                        case "Espontânea":
                                            enumAberturaOcular = EnumAberturaOcular.ESPONTANEA;
                                            break;
                                    }

                                    EnumRespostaVerbal enumRespostaVerbal = null;
                                    switch (resultVitima.getString("resposta_verbal")) {
                                        case "Ausente":
                                            enumRespostaVerbal = EnumRespostaVerbal.AUSENTE;
                                            break;
                                        case "Incompreensível":
                                            enumRespostaVerbal = EnumRespostaVerbal.INCOMPREENSIVEL;
                                            break;
                                        case "Desconexo":
                                            enumRespostaVerbal = EnumRespostaVerbal.DESCONEXO;
                                            break;
                                        case "Confuso":
                                            enumRespostaVerbal = EnumRespostaVerbal.CONFUSO;
                                            break;
                                        case "Orientado":
                                            enumRespostaVerbal = EnumRespostaVerbal.ORIENTADO;
                                            break;
                                    }

                                    EnumRespostaMotora enumRespostaMotora = null;
                                    String result1 = resultVitima.getString("resposta_motora");
                                    if (result1.matches("Au.*")) {
                                        enumRespostaMotora = EnumRespostaMotora.AUSENTE;
                                    } else if (result1.matches("Ex.*")) {
                                        enumRespostaMotora = EnumRespostaMotora.EXTENSAO;
                                    } else if (result1.matches("F.*")) {
                                        enumRespostaMotora = EnumRespostaMotora.FLEXAO;
                                    } else if (result1.matches("Ret.*")) {
                                        enumRespostaMotora = EnumRespostaMotora.RETIRADA_A_DOR;
                                    } else if (result1.matches("Ap.*")) {
                                        enumRespostaMotora = EnumRespostaMotora.APROPRIADA_A_DOR;
                                    } else if (result1.matches("Ob.*")) {
                                        enumRespostaMotora = EnumRespostaMotora.RESPONDE_COMANDOS;
                                    }

                                    String destinoVitima = "";
                                    String localDestino = resultVitima.getString("localdestino").replaceAll("['<>\\|/]", " ");
                                    localDestino = localDestino.toUpperCase();  //Converte a string toda para maiusculo

                                    boolean insertHospital = false;
                                    boolean existEncaminhamento = false;

                                    // Aqui onde o bixo pega, nÃ£o existe padrÃ£o no campo e nem na informaÃ§Ã£o...
                                    //EntÃ£o o jeito Ã© pegar o que conhecemos... :(
                                    if (localDestino.matches("LIBERAD.*")) {
                                        //Nesse caso o encaminhamento foi listado errado no formulÃ¡rios dos bombeiros
                                        destinoVitima = "Liberado no local";

                                    } else {
                                        if (localDestino.contains("RECU") || localDestino.contains("RESCU")) {
                                            //Nesse caso o encaminhamento foi listado errado no formulÃ¡rios dos bombeiros
                                            if (localDestino.contains("ENCAM")) {
                                                destinoVitima = "Recusou encaminhamento hospital";

                                            } else {
                                                destinoVitima = "Recusou atendimento";

                                            }
                                        } else {
                                            /*
                                             * HU
                                             * H U
                                             * Hospital UniversitÃ¡rio
                                             */
                                            Pattern patternHospital = Pattern.compile("[H][A-Z]*\\s?[U][A-Z]*|[H][A-Z]*\\s*[R][A-Z]* | UNIVER[A-Z]*");
                                            if (patternHospital.matcher(localDestino).find()) {
                                                localDestino = "HOSPITAL UNIVERSITÁRIO DO OESTE DO PARANÁ";
                                                destinoVitima = "Entregue no hospital";
                                            } else {
                                                /*
                                                 * UPA
                                                 * U P A
                                                 * Unidade de pronto atendimento
                                                 */
                                                patternHospital = Pattern.compile("[U][A-Z]*\\s*[P][A-Z0-9]* | PAC");
                                                if (patternHospital.matcher(localDestino).find()) {
                                                    if (localDestino.contains("3") || localDestino.contains("III") || localDestino.contains("LLL") || localDestino.toUpperCase().contains("VENE")) {
                                                        localDestino = "UPA 3";
                                                        destinoVitima = "Entregue no hospital";
                                                    } else {
                                                        if (localDestino.contains("2") || localDestino.contains("II") || localDestino.contains("LL") || localDestino.toUpperCase().contains("BRAS")) {
                                                            localDestino = "UPA 2";
                                                            destinoVitima = "Entregue no hospital";
                                                        } else {
                                                            if (localDestino.contains("1") || localDestino.contains("I") || localDestino.contains("L") || localDestino.toUpperCase().contains("PEDIA")) {
                                                                localDestino = "UPA 1";
                                                                destinoVitima = "Entregue no hospital";
                                                            } else {
                                                                localDestino = "NÃO INFORMADO";
                                                                destinoVitima = "Não informado";
                                                            }
                                                        }
                                                    }
                                                    //Finalizado os tratamentos de UPA
                                                } else {
                                                    /*
                                                     * SÃ£o Lucas
                                                     * Hospital SÃ£o Lucas
                                                     */
                                                    if (localDestino.contains("LUCAS")) {
                                                        localDestino = "HOSPITAL SÃO LUCAS";
                                                        destinoVitima = "Entregue no hospital";
                                                    } else {
                                                        if (localDestino.contains("CATARINA") || localDestino.contains("STA")) {
                                                            localDestino = "HOSPITAL E MATERNIDADE SANTA CATARINA";
                                                            destinoVitima = "Entregue no hospital";
                                                        } else {
                                                            if (localDestino.contains("SALETE")) {
                                                                localDestino = "HOSPITAL NOSSA SENHORA DA SALETE";
                                                                destinoVitima = "Entregue no hospital";
                                                            } else {
                                                                if (localDestino.contains("POLI")) {
                                                                    localDestino = "HOSPITAL POLICLÍNICA DE CASCAVEL";
                                                                    destinoVitima = "Entregue no hospital";
                                                                } else {
                                                                    if (localDestino.contains("LIMA")) {
                                                                        localDestino = "HOSPITAL E MATERNIDADE DOUTOR LIMA";
                                                                        destinoVitima = "Entregue no hospital";
                                                                    } else {
                                                                        if (localDestino.contains("IML") || localDestino.contains("LEGAL")) {
                                                                            localDestino = "IML";
                                                                            destinoVitima = "Entregue no hospital";
                                                                        } else {
                                                                            localDestino = "NÃO INFORMADO";
                                                                            destinoVitima = "Não informado";
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    String tipoVeiculo = "Não informado";
                                    /*Se o envolvido for condutor, informar o tipo de veiculo*/

                                    for (TempVeiculo veiculo : listVeiculos_aux) {
                                        //tenso que os caras erram o nome aqui rsrs...
                                        //escrevem de um jeito na RAS e no cadastro de veiculo de outro Â¬Â¬
                                        try {
                                            String[] nome = resultVitima.getString("nome_vitima").split(" ");
                                            String nome1 = nome[0] + " " + nome[1];

                                            if (veiculo.getCondutor().equalsIgnoreCase(nome1)) {
                                                tipoVeiculo = veiculo.getTipoVeiculo();
                                                break;
                                            }
                                        } catch (Exception ex) {
                                            if (veiculo.getCondutor().equalsIgnoreCase(resultVitima.getString("nome_vitima"))) {
                                                tipoVeiculo = veiculo.getTipoVeiculo();
                                                break;

                                            }
                                        }
                                    }

                                    long situa = resultVitima.getInt("situa_vitima");
                                    for (TempVeiculo veiculo : listVeiculos_aux) {
                                        if (situa == veiculo.getRgo_veiculo()) {
                                            tipoVeiculo = veiculo.getTipoVeiculo();
                                        }
                                    }

                                    String tiposituacao = "";
                                    if (situa == 0) {
                                        tiposituacao = "A pé";
                                    } else {
                                        if (tipoVeiculo.matches("Auto.*")) {
                                            tiposituacao = "Em auto";
                                        } else {
                                            if (tipoVeiculo.matches("Bici.*")) {
                                                tiposituacao = "Em bicicleta";
                                            } else {
                                                if (tipoVeiculo.matches("Mot.*")) {
                                                    tiposituacao = "Em moto";
                                                } else {
                                                    if (tipoVeiculo.matches("On.*")) {
                                                        tiposituacao = "Em ônibus";
                                                    } else {
                                                        tiposituacao = "Não informado";
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    
                                    String Insert_vitimaRAS = "Insert into formularios.vitimaras (aberturaocular,atendimentomedicoacompanhamentohospital,"
                                            + "atendimentomedicocompareceu,atendimentomedicointervencaomedica,"
                                            + "atendimentomedicosolicitado,datacriacao,enderecovitima,escalacoma"
                                            + ",escalaglasgow,escalacomafinal,escalatraumafinal,frequenciarespfinal,idade"
                                            + ",nomevitima,numerovitima,observacao,pafinal,pamaxima,pulsofinal,respiracaominuto"
                                            + ",respostamotora,respostaverbal, sao2final, sexo, socorristareponsavel, tipocondicaodeseguranca"
                                            + ", tipoposicaoveiculo, viasareas, usuario_login, destinodavitima, hospital, ras_id, "
                                            + "tiposituacao, tipoveiculo, sinaisclinicos, procedimentos, cranio, face, pescoco"
                                            + ", dorso, torax, abdomen, pelvis, membro_sup_direito, membro_sup_esquerdo, membro_inf_esquerdo"
                                            + ", membro_inf_direito) values (";
                                    
                                    String updateVitima = "UPDATE formularios.vitimaras SET ";

                                    //InserÃ§Ã£o da vÃ­tima
                                    updateVitima += "aberturaocular=\'" + enumAberturaOcular + "\'";
                                    Insert_vitimaRAS += "\'" + enumAberturaOcular + "\',";
                                    updateVitima += ", atendimentomedicoacompanhamentohospital=" + (resultVitima.getString("medicohospital").equalsIgnoreCase("sim") ? true : false);
                                    Insert_vitimaRAS += (resultVitima.getString("medicohospital").equalsIgnoreCase("sim") ? true : false) + ",";
                                    updateVitima += ", atendimentomedicocompareceu=" + (resultVitima.getString("medicocomparece").equalsIgnoreCase("sim") ? true : false);
                                    Insert_vitimaRAS += (resultVitima.getString("medicocomparece").equalsIgnoreCase("sim") ? true : false) + ",";
                                    updateVitima += ", atendimentomedicointervencaomedica=" + (resultVitima.getString("medicointerveio").equalsIgnoreCase("sim") ? true : false);
                                    Insert_vitimaRAS += (resultVitima.getString("medicointerveio").equalsIgnoreCase("sim") ? true : false) + ",";
                                    updateVitima += ", atendimentomedicosolicitado=" + (resultVitima.getString("medicosolicitado").equalsIgnoreCase("sim") ? true : false);
                                    Insert_vitimaRAS += (resultVitima.getString("medicosolicitado").equalsIgnoreCase("sim") ? true : false) + ",";
                                    updateVitima += ", datacriacao=\'" + new java.sql.Date(new Date().getTime()) + "\'"; //Data de criacao
                                    Insert_vitimaRAS += "\'" + new java.sql.Date(new Date().getTime()) + "\',";
                                    updateVitima += ", enderecovitima=\'\'";  //endereÃ§o vitima
                                    Insert_vitimaRAS += "\'\',";
                                    updateVitima += ", escalacoma=\'" + EnumEscalaComa._0 + "\'";
                                    Insert_vitimaRAS += "\'" + EnumEscalaComa._0 + "\',";
                                    updateVitima += ", escalaglasgow=\'" + Integer.parseInt(resultVitima.getString("escalaglasgow")) + "\'";
                                    Insert_vitimaRAS += "\'" + Integer.parseInt(resultVitima.getString("escalaglasgow")) + "\',";
                                    updateVitima += ", escalacomafinal=\'\'";
                                    Insert_vitimaRAS += "\'\',";
                                    updateVitima += ", escalatraumafinal=\'" + resultVitima.getString("escalatrauma") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("escalatrauma") + "\',";
                                    updateVitima += ", frequenciarespfinal=\'" + resultVitima.getString("freqrespiracao").replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("freqrespiracao").replaceAll("['<>\\|/]", " ") + "\',";
                                    updateVitima += ", idade=\'" + Integer.parseInt(resultVitima.getString("idade")) + "\'";
                                    Insert_vitimaRAS += "\'" + Integer.parseInt(resultVitima.getString("idade")) + "\',";
                                    updateVitima += ", nomevitima=\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\',";
                                    updateVitima += ", numerovitima=" + countVitima;
                                    Insert_vitimaRAS += countVitima+ ",";
                                    updateVitima += ", observacao=\'" + resultVitima.getString("lesao") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("lesao") + "\',";
                                    updateVitima += ", pafinal=\'" + resultVitima.getString("pressaoarterial") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("pressaoarterial") + "\',";
                                    updateVitima += ", pamaxima=\'" + EnumPAMaxima._0 + "\'";
                                    Insert_vitimaRAS += "\'" + EnumPAMaxima._0 + "\',";
                                    updateVitima += ", pulsofinal=\'" + resultVitima.getString("pulso").replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("pulso").replaceAll("['<>\\|/]", " ") + "\',";
                                    updateVitima += ", respiracaominuto=\'" + EnumRespiracaoMinuto._0 + "\'";
                                    Insert_vitimaRAS += "\'" + EnumRespiracaoMinuto._0 + "\',";
                                    updateVitima += ", respostamotora=\'" + enumRespostaMotora + "\'";
                                    Insert_vitimaRAS += "\'" + enumRespostaMotora + "\',";
                                    updateVitima += ", respostaverbal=\'" + enumRespostaVerbal + "\'";
                                    Insert_vitimaRAS += "\'" + enumRespostaVerbal + "\',";
                                    updateVitima += ", sao2final=\'" + resultVitima.getString("sao2").replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("sao2").replaceAll("['<>\\|/]", " ") + "\',";
                                    updateVitima += ", sexo=\'" + sexo + "\'";
                                    Insert_vitimaRAS += "\'" + sexo + "\',";
                                    updateVitima += ", socorristareponsavel=\'" + resultVitima.getString("respvitima").replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + resultVitima.getString("respvitima").replaceAll("['<>\\|/]", " ") + "\',";
                                    updateVitima += ", tipocondicaodeseguranca=\'" + enumCondicaoSeguranca + "\'";
                                    Insert_vitimaRAS += "\'" + enumCondicaoSeguranca + "\',";
                                    updateVitima += ", tipoposicaoveiculo=\'" + enumPosicaoNoVeiculo + "\'";
                                    Insert_vitimaRAS += "\'" + enumPosicaoNoVeiculo + "\',";
                                    updateVitima += ", viasareas=" + false + "";
                                    Insert_vitimaRAS +=  false + ",";
                                    updateVitima += ", usuario_login=\'SISTEMA\'"; // Usuario criador -> SISTEMA 
                                    Insert_vitimaRAS += "\'SISTEMA\',";
                                    updateVitima += ", destinodavitima=\'" + destinoVitima + "\'";
                                    Insert_vitimaRAS += "\'" + destinoVitima + "\',";
                                    updateVitima += ", hospital=\'" + localDestino + "\'";
                                    Insert_vitimaRAS += "\'" + localDestino + "\',";
                                    updateVitima += ", ras_id=\'" + id_ras + "\'";
                                    Insert_vitimaRAS += "\'" + id_ras + "\',";
                                    updateVitima += ", tiposituacao=\'" + tiposituacao + "\'";
                                    Insert_vitimaRAS += "\'" + tiposituacao + "\',";
                                    updateVitima += ", tipoveiculo=\'" + tipoVeiculo + "\'";
                                    Insert_vitimaRAS += "\'" + tipoVeiculo + "\',";

                                    //Pegando Sinais clinicos...
                                    String sinaisClinicos = resultVitima.getString("array_sinaisclinicos");
                                    if (!sinaisClinicos.equals("")) {
                                        String[] arraySinaisClinicos = sinaisClinicos.split(",");
                                        for (int i = 0; i < arraySinaisClinicos.length; i++) {
//                            System.out.println("INSERT INTO tiposinalclinico (descricao) VALUES(\'" + arraySinaisClinicos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                           Atualiza.executeSQL_sigetrans("INSERT INTO tiposinalclinico (descricao) VALUES(\'" + arraySinaisClinicos[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }

                                    updateVitima += ", sinaisclinicos=\'" + sinaisClinicos.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + sinaisClinicos.replaceAll("['<>\\|/]", " ") + "\',";

                                    //Pegando procedimentos...
                                    String procedimentos = resultVitima.getString("array_procedimentos");
                                    if (!procedimentos.equals("")) {
                                        String[] arrayProcedimentos = procedimentos.split(", ");
                                        for (int i = 0; i < arrayProcedimentos.length; i++) {
//                            System.out.println("INSERT INTO tipoprocedimento (descricao) VALUES(\'" + arrayProcedimentos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO tipoprocedimento (descricao) VALUES(\'" + arrayProcedimentos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                            vitimaRAS.getProcedimentos().add(new TipoProcedimento(arrayProcedimentos[i]));                      
                                        }
                                    }
                                    updateVitima += ", procedimentos=\'" + procedimentos.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + procedimentos.replaceAll("['<>\\|/]", " ") + "\',";

                                    String lesao = resultVitima.getString("cranio");
                                    String[] vet = lesao.split(",");

                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }

                                    //cranio
                                    updateVitima += ", cranio=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("face");
                                    vet = lesao.split(", ");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }

                                    //face
                                    updateVitima += ", face=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("pescoco");
                                    vet = lesao.split(", ");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");

                                        }
                                    }
                                    //Pescoco
                                    updateVitima += ", pescoco=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("dorso");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");

                                        }
                                    }
                                    //dorso
                                    updateVitima += ", dorso=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("torax");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }
                                    //torax
                                    updateVitima += ", torax=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("abdomen");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }
                                    //abdomen
                                    updateVitima += ", abdomen=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("pelvis");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }
                                    //pelvis
                                    updateVitima += ", pelvis=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("msd");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }
                                    //msd
                                    updateVitima += ", membro_sup_direito=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("mse");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }

                                    //mse
                                    updateVitima += ", membro_sup_esquerdo=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("mie");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                            Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }

                                    //mie
                                    updateVitima += ", membro_inf_esquerdo=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\'";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\',";

                                    lesao = resultVitima.getString("mid");
                                    vet = lesao.split(",");
                                    if (vet.length > 0 && !vet[0].equals("")) {
                                        for (int i = 0; i < vet.length; i++) {
                                            if (vet[i].startsWith(" ")) {
                                                vet[i] = "" + vet[i].subSequence(1, vet[i].length());
                                            }
//                            System.out.println("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
//                             Atualiza.executeSQL_sigetrans("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                                        }
                                    }
                                    //mid
                                    updateVitima += ", membro_inf_direito=\'" + lesao.replaceAll("['<>\\|/]", " ") + "\' where ras_id=" + id_ras + " and numerovitima=" + countVitima + " returning id";
                                    Insert_vitimaRAS += "\'" + lesao.replaceAll("['<>\\|/]", " ") + "\') returning id";
                                    
                                    System.out.println(updateVitima);
                                    ResultSet rsUpdate = null;
                                    rsUpdate = Atualiza.Consulta_sigetrans(updateVitima);
                                    Long id_vitimaRAS=null;
                                    
                                    if (rsUpdate.next()){
                                        id_vitimaRAS = rsUpdate.getLong("id");
                                    }else{
                                        rsUpdate = Atualiza.Consulta_sigetrans(Insert_vitimaRAS);
                                        rsUpdate.next();
                                        id_vitimaRAS = rsUpdate.getLong("id");
                                    }
                                    
                                    /*Verifica (usando o nome) se a pessoa jÃ¡ estÃ¡ cadastrada. Caso esteja, um novo registro vinculado Ã© criado */
//                    System.out.println("SELECT id, nome from pessoa where nome=\'" + vitimaRAS.getNomeVitima().replaceAll("['<>\\|/]", " ") + "\'");
                                    ResultSet pessoasResult = null;
                                    pessoasResult = Atualiza.Consulta_sigetrans("SELECT id, nome from pessoa where nome=\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\'");

                                    Long id_pessoa = null;

                                    if (pessoasResult.next()) {

                                        id_pessoa = pessoasResult.getLong("id");

                                        String UpdateEnvolvido = "UPDATE public.envolvido SET ";
                                        UpdateEnvolvido += "ativo=\'" + envolvidoAtivo + "\'";
                                        UpdateEnvolvido += ", condicaoseguranca=\'" + EnumCondicaoSeguranca.NAO_OBSERVADO + "\'";
                                        UpdateEnvolvido += ", datacriacao=\'" + new java.sql.Date(new Date().getTime()) + "\'";
                                        UpdateEnvolvido += ", idadeanos=\'" + Integer.parseInt(resultVitima.getString("idade")) + "\'";
                                        UpdateEnvolvido += ", posicaonoveiculo=\'" + enumPosicaoNoVeiculo + "\'";
                                        UpdateEnvolvido += ", tiporegistro=\'vitima\'";
                                        UpdateEnvolvido += ", situacao=\'Não informado\'";
                                        UpdateEnvolvido += ", tipoveiculo=\'" + tipoVeiculo + "\'";
                                        UpdateEnvolvido += ", profissao=\'" + new TipoProfissao().getDescricao() + "\'";
                                        UpdateEnvolvido += ", escolaridade=\'" + EnumEscolaridade.NAO_INFORMADO + "\'";
                                        UpdateEnvolvido += ", estadocivil=\'" + EnumSituacaoCivil.NAO_INFORMADO + "\'";
                                        UpdateEnvolvido += ", acidentedetrabalho=\'" + false + "\'";
                                        UpdateEnvolvido += ", vitimaras_id=\'" + id_vitimaRAS + "\' where pessoa_id=" + id_pessoa + " and ocorrencia_id=" + id_ocorrencia + " returning condutor_id";
//                                    System.out.println(UpdateEnvolvido);
                                        ResultSet update_envolvido = Atualiza.Consulta_sigetrans(UpdateEnvolvido);
                                        update_envolvido.next();
//                                    Long id_condutor = update_envolvido.getLong("condutor_id");

//                                    for (TempVeiculo veiculo : listVeiculos) {
//                                        if (veiculo.getCondutor().equals(resultVitima.getString("nome_vitima"))) {
//                                            ResultSet resultCondutor = Atualiza.Consulta_sigetrans("UPDATE public.condutor SET sintomasembriaguez=false, solicitacaoexame=false, estadocondutor='NAO_INFORMADO', categoriahabilitacao='NAO_INFORMADO', situacaohabilitacao='NAO_INFORMADO', tipohabilitacao='NAO_INFORMADO' where id=" + id_condutor + " returning veiculo_id;");
//                                            resultCondutor.next();
//                                            Long id_veiculo = resultCondutor.getLong("veiculo_id");
//
//                                            ResultSet resultV = Atualiza.Consulta_sigetrans("Update public.veiculo SET cidade=\'" + veiculo.getMunicipio().replaceAll("['<>\\|/]", " ") + "\', placa=\'" + veiculo.getPlaca() + "\' where id=" + id_veiculo);
//                                            resultV.next();
//                                            listVeiculos.remove(veiculo);
//                                            break;
//                                        }
//                                    }
                                    } else {
                                        System.out.println("Não existe Pessoa cadastrada");
                                    }

                                }
                            }
                        } else {
                            System.out.println("Vítima de id_rgo_vitima =" + result[0] + " update impedido");
                        }
                    } catch (PSQLException p) {
                        p.printStackTrace();
                        System.exit(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(11);
                    }

                }
                System.gc();
            }
            Atualiza.Conexao.commit();
            System.gc();
//            String sql = "UPDATE sysbmccb.sigetrans_export_metadata SET oper='x' where id="+bombeiros.getID();
//            Atualiza.Update_bombeiros(sql);
            count_max++;
        }

        System.out.println(
                "Fim da atualizacao");
        Atualiza.close_bombeiros();

        Atualiza.close_sigetrans();
    }

    private static Endereco processaEndereco(String rua, String cruzamento, String cep, String bairro, String cidade) {
        boolean consultaGeo = false;
        if (rua.contains("-")) {
            String[] resultSet = rua.split("-");
            //Pegando o logradouro primeiro
            rua = resultSet[0];

            if (resultSet[1].contains("de ")) {
                resultSet = resultSet[1].split("de");
                //salvando o numero
                //Esse passo cria algo como <logradouro> <numero>
                //se chegou aqui, deve ser referenciada...
                if (resultSet[1].contains("ao")) {
                    rua += " " + resultSet[1].split("ao")[0];
                    consultaGeo = true;
                } else {
                    rua += " " + resultSet[1];
                    consultaGeo = true;
                }
            } else {
                if (resultSet[1].contains("até")) {
                    rua += " " + resultSet[1].split("até")[1];
                    consultaGeo = true;
                }
            }
        }
        Endereco end = new Endereco();

        if (consultaGeo && !rua.contains("PR") && !rua.contains("BR")) {
            //Pode ser georreferenciado...
            end = GeoCoderGoogle.queryAddress(FuncoesEstaticas.removeAccents(rua));
            totalRegistrosGeo++;
        } else {
            end.setRua(rua);
        }

        if (cruzamento == null || cruzamento.equals("")) {
            end.setCruzamento(false);
            end.setCruzamentoCom("");
        } else {
            end.setCruzamento(true);
            end.setCruzamentoCom(cruzamento);
        }
        end.setRua(end.getRua().replaceAll("['<>\\|/]", " "));
        end.setCruzamentoCom(end.getCruzamentoCom().replaceAll("['<>\\|/]", " "));
        end.setCidade(cidade);
        end.setBairro(bairro);
        end.setCep(cep);
        return end;
    }

    private static Endereco processaEndereco(String rua, String numero, String cep, String bairro, String cidade, String cruzamento) {
        rua += " " + numero;
        Endereco end = new Endereco();

        if (!rua.contains("PR") && !rua.contains("BR")) {
            //Pode ser georreferenciado...
            end = GeoCoderGoogle.queryAddress(FuncoesEstaticas.removeAccents(rua));
            totalRegistrosGeo++;
        } else {
            end.setRua(rua);
        }

        if (cruzamento == null || cruzamento.equals("")) {
            end.setCruzamento(false);
            end.setCruzamentoCom("");
        } else {
            end.setCruzamento(true);
            end.setCruzamentoCom(cruzamento);
        }
        end.setRua(end.getRua().replaceAll("['<>\\|/]", " "));
        end.setCruzamentoCom(end.getCruzamentoCom().replaceAll("['<>\\|/]", " "));
        end.setCidade(cidade);
        end.setBairro(bairro);
        end.setCep(cep);
        return end;
    }

    private static boolean isDigit(String s) {

        // cria um array de char  
        char[] c = s.toCharArray();
        boolean d = true;

        for (int i = 0; i < c.length; i++) // verifica se o char não é um dígito  
        {
            if (!Character.isDigit(c[i])) {
                d = false;
                break;
            }
        }

        return d;
    }

    private static Date processDate(String timestamp, boolean date) {
        String[] data = timestamp.split(" ");

        if (date) {
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dateFormated = sdf.parse(data[0]);
                return dateFormated;

            } catch (ParseException ex) {
                Logger.getLogger(InsertOrUpdate.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Date dateFormated = sdf.parse(data[1]);
                //System.out.println(dateFormated);
                return dateFormated;

            } catch (ParseException ex) {
                Logger.getLogger(InsertOrUpdate.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static String preparaInsertOcorrencia(boolean coordenadas) {
        if (coordenadas) {
            return "INSERT INTO ocorrencia("
                    + "dataacidente,"
                    + "horarioacidente,"
                    + "tipoacidente, "
                    + "clima, "
                    + "conservacao, "
                    + "pavimentacao, "
                    + "perfilpista, "
                    + "provavelcausa, "
                    + "severidade, "
                    + "superficie, "
                    + "bairro, "
                    + "cep, "
                    + "cidade,"
                    + "coordenadas, "
                    + "numero,"
                    + "rua, "
                    + "cruzamento, "
                    + "cruzamentocom, "
                    + "usuario_login,"
                    + "datacriacao,"
                    + "observacao,"
                    + "sinalizacao)"
                    + " VALUES ";
        } else {
            return "INSERT INTO ocorrencia("
                    + "dataacidente, "
                    + "horarioacidente,"
                    + "tipoacidente, "
                    + "clima, "
                    + "conservacao, "
                    + "pavimentacao, "
                    + "perfilpista, "
                    + "provavelcausa, "
                    + "severidade, "
                    + "superficie, "
                    + "bairro, "
                    + "cep, "
                    + "cidade, "
                    + "numero, "
                    + "rua, "
                    + "cruzamento, "
                    + "cruzamentocom, "
                    + "usuario_login, "
                    + "datacriacao, "
                    + "observacao,"
                    + "sinalizacao)"
                    + " VALUES ";
        }
    }

    private static String preparaConsultaVitimaRAS() {
        return "INSERT INTO formularios.vitimaras("
                + "aberturaocular,"
                + "atendimentomedicoacompanhamentohospital, "
                + "atendimentomedicocompareceu, "
                + "atendimentomedicointervencaomedica, "
                + "atendimentomedicosolicitado, "
                + "datacriacao, "
                + "enderecovitima, "
                + "escalacoma, "
                + "escalaglasgow, "
                + "escalacomafinal, "
                + "escalatraumafinal, "
                + "frequenciarespfinal,"
                + "idade, "
                + "nomevitima,"
                + "numerovitima, "
                + "observacao, "
                + "pafinal, "
                + "pamaxima,"
                + "pulsofinal,"
                + "respiracaominuto,"
                + "respostamotora,"
                + "respostaverbal,"
                + "sao2final,"
                + "sexo,"
                + "socorristareponsavel,"
                + "tipocondicaodeseguranca,"
                + "tipoposicaoveiculo,"
                + "viasareas,"
                + "usuario_login,"
                + "destinodavitima,"
                + "hospital,"
                + "ras_id,"
                + "tiposituacao,"
                + "tipoveiculo,"
                + "sinaisclinicos,"
                + "procedimentos,"
                + "cranio,"
                + "face,"
                + "pescoco,"
                + "dorso,"
                + "torax,"
                + "abdomen,"
                + "pelvis,"
                + "membro_sup_direito,"
                + "membro_sup_esquerdo,"
                + "membro_inf_esquerdo,"
                + "membro_inf_direito)"
                + " VALUES ";

    }

    private static String preparaConsultaRAS(boolean coordenadas) {
        if (coordenadas) {
            return "INSERT INTO formularios.ras("
                    + "rgo,"
                    + "dataacidente,"
                    + "horarioacidente,"
                    + "tipoacidente, "
                    + "bairro, "
                    + "cep, "
                    + "cidade,"
                    + "coordenadas, "
                    + "numero,"
                    + "rua, "
                    + "cruzamento, "
                    + "cruzamentocom, "
                    + "usuario_login,"
                    + "datacriacao,"
                    + "ocorrencia_id)"
                    + " VALUES ";
        } else {
            return "INSERT INTO formularios.ras("
                    + "rgo,"
                    + "dataacidente,"
                    + "horarioacidente,"
                    + "tipoacidente, "
                    + "bairro, "
                    + "cep, "
                    + "cidade, "
                    + "numero, "
                    + "rua, "
                    + "cruzamento, "
                    + "cruzamentocom, "
                    + "usuario_login, "
                    + "datacriacao, "
                    + "ocorrencia_id)"
                    + " VALUES ";
        }
    }

    private static String preparaConsultaPessoa() {
        return "INSERT INTO pessoa("
                + "nome, "
                + "rua)"
                + " VALUES ";
    }

    private static String preparaConsultaEnvolvido() {
        return "INSERT INTO envolvido("
                + "ativo,"
                + "condicaoseguranca,"
                + "datacriacao,"
                + "idadeanos,"
                + "posicaonoveiculo,"
                + "tiporegistro,"
                + "usuario_login,"
                + "pessoa_id,"
                + "ocorrencia_id, "
                + "situacao, "
                + "tipoveiculo, "
                + "profissao, "
                + "escolaridade, "
                + "estadocivil, "
                + "acidentedetrabalho, "
                + "condutor_id, "
                + "vitimaras_id)"
                + " VALUES ";
    }

    public static boolean verificaEnvolvidoAtivo(Date dataOcorrencia) {
        Date periodoOMS = FuncoesEstaticas.decrementDate(new Date(), 30);
        return periodoOMS.compareTo(dataOcorrencia) >= 0 ? false : true;
    }
}
