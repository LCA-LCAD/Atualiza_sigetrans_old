/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package atualiza_sigetrans;

import atualiza_sigetrans.domain.Endereco;
import atualiza_sigetrans.enumerate.EnumRespiracaoMinuto;
import atualiza_sigetrans.enumerate.EnumPAMaxima;
import atualiza_sigetrans.enumerate.EnumRespostaMotora;
import atualiza_sigetrans.enumerate.EnumEscalaComa;
import atualiza_sigetrans.enumerate.EnumSexo;
import atualiza_sigetrans.enumerate.EnumSituacaoCivil;
import atualiza_sigetrans.enumerate.EnumRespostaVerbal;
import atualiza_sigetrans.enumerate.EnumEscolaridade;
import atualiza_sigetrans.enumerate.EnumCondicaoSeguranca;
import atualiza_sigetrans.enumerate.EnumAberturaOcular;
import atualiza_sigetrans.enumerate.EnumPosicaoNoVeiculo;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Gabriel
 */
public class Atualiza_sigetrans {

    public static int totalRegistrosGeo = 0;
    public static int totalRegistrosRGOLidos = 0;

    /**
     * @param args the command line arguments
     */
    public static void a(String[] args) throws Exception {

        RemoteCon.set("201.41.143.66", 6304, "sysbmccb", "SIGETRANS", "cotrans193#");

        //Tempo para aguardar a reconexao...
        RemoteCon.timeout = 1000;
//////
//////        //Log de erros em arquivo...
        atualiza_sigetrans.Logger.store = true;
//////        //Armazena todos os erros (true) ou somente o Ãºltimo (false)
//////
        atualiza_sigetrans.Logger.allErrors = true;
        atualiza_sigetrans.Logger.fileLimit = 5000000;
////
//
        System.out.println("conectou nos bombeiros?: " + RemoteCon.connect());
////                       
        Long id_ras;
        Long id_ocorrencia;
        boolean envolvidoAtivo = false;

        //Apesar de possuir hibertante, neste contexto nÃ£o existe uma sessÃ£o...
        LocalCon.set("localhost", 5432, "SIGETRANS", "postgres", "postgres");
        System.out.println("conectou nos SIGETRANS?: " + LocalCon.connect());

        String queryRGO_SQL = preparaConsultaRGO();
        //System.out.println(queryRGO_SQL);

        List<MapeamentoDeChaves> listOcorrencias = new ArrayList<>();
        ResultSet rs = RemoteCon.executeQuery(queryRGO_SQL);

        //Puxando todas as ocorrencias...
        while (rs.next()) {
            System.out.println("Ocorrencias até o momento " + totalRegistrosRGOLidos);

            String insertOcorrencia;
            String cod_rgo = rs.getString("cod_rgo");
            Date data = processDate(rs.getString("datahora"), true);
            Date horario = processDate(rs.getString("datahora"), false);


            /*Verifica a rua e tenta colocar no padrÃ£o google maps do SIGETRANS
             Se o endereÃ§o nÃ£o contiver um nÃºmero aproximado, a consulta ao google maps serÃ¡ ignorada
             e o endereÃ§o obtido na base dos bombeiros sera usado */
            Endereco end = processaEndereco(rs.getString("endereco_geral.log_nome"), rs.getString("cruzamento"));

            /*Usar o replaceAll para campos que nÃ£o possuem valores prÃ©-definidos na base dos bombeiros
             isso evita erros de inserÃ§Ã£o (Erros de SQL) por conter caracteres especiais*/
            String observacoes = rs.getString("referencia").replaceAll("['<>\\|/]", " ");

            String tipoAcidente = rs.getString("ocorrencia");

            if (tipoAcidente.contains("-")) {
                tipoAcidente = tipoAcidente.split("- ")[1];
            }

            String tipoCausa = rs.getString("causa").replaceAll("['<>\\|/]", " ");

            //Verifica se existe o tipo no banco
            ResultSet consulta = LocalCon.executeQuery("SELECT descricao FROM tipocausa WHERE descricao=\'" + tipoCausa + "\'");
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

            //Monta a SQL de inserÃ§Ã£o de ocorrencia considerando quando tem coordenadas ou nÃ£o
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

//            System.out.println("SQL: " + insertOcorrencia);
            ResultSet rsInsert = LocalCon.executeQuery(insertOcorrencia);
            rsInsert.next();
            id_ocorrencia = rsInsert.getLong("id");

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

//            System.out.println("SQL insert RAS " + insertRAS);
            ResultSet rs2 = LocalCon.executeQuery(insertRAS);
            rs2.next();
            id_ras = rs2.getLong("id");

            envolvidoAtivo = verificaEnvolvidoAtivo(data);
            listOcorrencias.add(new MapeamentoDeChaves(id_ocorrencia, id_ras, Long.parseLong(cod_rgo)));
            System.gc();
        }

        //Mapeou as ocorrencias, puxando as vitimas...     
        for (MapeamentoDeChaves ids : listOcorrencias) {
//           Inserido ocorrencia e RAS... agora a parte de vitima, pessoa, envolvido e historico

            /*A SQL ficou grande para reduzir a quantidade de informaÃ§Ã£o trafegando na rede, desta forma, Ã© carregado somente
             os campos necessÃ¡rios ao sigetrans.*/
            System.out.println(ids.getCod_rgo());
            String queryVitimaSQL = "select vitima as nome_vitima, idade, sexo, tab_lesao.tipo as lesao, tab_posicao.nome as posicao_veiculo,"
                    + " tab_seguranca.nome as condicao_seguranca, tab_ocular.nome as abertura_ocular, pressaoarterial,"
                    + " tab_verbal.nome as resposta_verbal, tab_motora.nome as resposta_motora, recusa, pulso, sao2,"
                    + " freqrespiracao, array_sinaisclinicos, sinaisclinicos, tab_encaminhamento.nome as encaminhamento, localdestino,"
                    + " respvitima, medicosolicitado, medicocomparece, medicointerveio, medicohospital, escalatrauma, escalaglasgow,"
                    + " cranio, face, pescoco, dorso, torax, abdomen, pelvis, msd, mse, mid, mie, array_procedimentos"
                    + " from (select fk_rgo, vitima, idade, sexo, cod_lesao, recusa, situa_vitima, posicao, seguranca, aberturaocular, respverbal,"
                    + " respostamotora, pulso, sao2, pressaoarterial, freqrespiracao, array_sinaisclinicos, cranio, face, pescoco, dorso, torax,"
                    + " abdomen, pelvis, msd, mse, mid, mie, array_procedimentos, sinaisclinicos, destino, localdestino, respvitima, medicosolicitado,"
                    + " medicocomparece, medicointerveio, medicohospital, escalatrauma, escalaglasgow from rgo_vitima where fk_rgo=" + ids.getCod_rgo() + ")as tab_vitima"
                    + " inner join (select cod_encaminhamento, nome from ts_encaminhamento) as tab_encaminhamento on(cod_encaminhamento=destino)"
                    + " inner join (select cod_lesao, tipo from ts_lesao) as tab_lesao using(cod_lesao)"
                    + " inner join (select cod_posicionavit, nome from ts_posicionavit) as tab_posicao on(cod_posicionavit=posicao)"
                    + " inner join (select cod_segurancavit, nome from ts_segurancavit) as tab_seguranca on(cod_segurancavit=seguranca)"
                    + " inner join (select cod_ocular, nome from ts_ocularvit) as tab_ocular on(cod_ocular=aberturaocular)"
                    + " inner join (select cod_respverbal, nome from ts_respverbal) as tab_verbal on(cod_respverbal=respverbal)"
                    + " inner join (select cod_respmotora, nome from ts_respmotora) as tab_motora on(cod_respmotora=respostamotora)";

            /* Consultando sobre os veiculos envolvidos. Mais abaixo, sera usado para o cadastro de veiculos e envolvidos*/
            ResultSet veiculos = RemoteCon.executeQuery("select id_rgo_veiculo,placa, municipio, fk_tipotransporte, nome as tpveiculo, condutor from rgo_veiculos "
                    + "inner join ts_tipotransporte on (cod_tptransporte = fk_tipotransporte)"
                    + " where fk_rgo=\'" + ids.getCod_rgo() + "\'");

            List<TempVeiculo> listVeiculos = new ArrayList<>();

            while (veiculos.next()) {
                listVeiculos.add(new TempVeiculo(veiculos.getLong("id_rgo_veiculo"), veiculos.getString("placa"), veiculos.getString("municipio"),
                        veiculos.getString("condutor"), veiculos.getString("tpveiculo")));
            }

            try (ResultSet resultVitima = RemoteCon.executeQuery(queryVitimaSQL)) {
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
                    String result = resultVitima.getString("resposta_motora");
                    if (result.matches("Au.*")) {
                        enumRespostaMotora = EnumRespostaMotora.AUSENTE;
                    } else if (result.matches("Ex.*")) {
                        enumRespostaMotora = EnumRespostaMotora.EXTENSAO;
                    } else if (result.matches("F.*")) {
                        enumRespostaMotora = EnumRespostaMotora.FLEXAO;
                    } else if (result.matches("Ret.*")) {
                        enumRespostaMotora = EnumRespostaMotora.RETIRADA_A_DOR;
                    } else if (result.matches("Ap.*")) {
                        enumRespostaMotora = EnumRespostaMotora.APROPRIADA_A_DOR;
                    } else if (result.matches("Ob.*")) {
                        enumRespostaMotora = EnumRespostaMotora.RESPONDE_COMANDOS;
                    }

                    String destinoVitima = "";
                    String localDestino = resultVitima.getString("localdestino").replaceAll("['<>\\|/]", " ");
                    localDestino = localDestino.toUpperCase();  //Converte a string toda para maiusculo

                    boolean insertHospital = false;
                    boolean existEncaminhamento = false;

                    // Aqui onde o bixo pega, nÃ£o existe padrÃ£o no campo e nem na informaÃ§Ã£o...
                    //EntÃ£o o jeito Ã© pegar o que conhecemos... :(
                    if (localDestino.contains("LIBERAD")) {
                        //Nesse caso o encaminhamento foi listado errado no formulÃ¡rios dos bombeiros
                        destinoVitima = "Liberado no local";
                        existEncaminhamento = true;
                    } else {
                        if (localDestino.contains("RECU") || localDestino.contains("RESCU")) {
                            //Nesse caso o encaminhamento foi listado errado no formulÃ¡rios dos bombeiros
                            if (localDestino.contains("ENCAM")) {
                                destinoVitima = "Recusou encaminhamento hospital";
                                existEncaminhamento = true;
                            } else {
                                destinoVitima = "Recusou atendimento";
                                existEncaminhamento = true;
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
                            } else {
                                /*
                                 * UPA
                                 * U P A
                                 * Unidade de pronto atendimento
                                 */
                                patternHospital = Pattern.compile("[U][A-Z]*\\s*[P][A-Z0-9]* | PAC");
                                if (patternHospital.matcher(localDestino).find()) {
                                    if (localDestino.contains("3") || localDestino.contains("III") || localDestino.contains("LLL")) {
                                        localDestino = "UPA 3";
                                    } else {
                                        if (localDestino.contains("2") || localDestino.contains("II") || localDestino.contains("LL")) {
                                            localDestino = "UPA 2";
                                        } else {
                                            if (localDestino.contains("1") || localDestino.contains("I") || localDestino.contains("L")) {
                                                localDestino = "UPA 1";
                                            } else {
                                                localDestino = "NÃO INFORMADO";
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
                                    } else {
                                        if (localDestino.contains("CATARINA") || localDestino.contains("STA")) {
                                            localDestino = "HOSPITAL E MATERNIDADE SANTA CATARINA";
                                        } else {
                                            if (localDestino.contains("SALETE")) {
                                                localDestino = "HOSPITAL NOSSA SENHORA DA SALETE";
                                            } else {
                                                if (localDestino.contains("POLI")) {
                                                    localDestino = "HOSPITAL POLICLÍNICA DE CASCAVEL";
                                                } else {
                                                    if (localDestino.contains("LIMA")) {
                                                        localDestino = "HOSPITAL E MATERNIDADE DOUTOR LIMA";
                                                    } else {
                                                        if (localDestino.contains("IML") || localDestino.contains("LEGAL")) {
                                                            localDestino = "IML";
                                                        } else {
                                                            localDestino = "NÃO INFORMADO";
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
                    if (enumPosicaoNoVeiculo.equals(EnumPosicaoNoVeiculo.CONDUTOR)) {
                        /*
                         * Buscar os veiculos atÃ© encontrar o condutor correto.
                         */
                        for (TempVeiculo veiculo : listVeiculos) {
                            //tenso que os caras erram o nome aqui rsrs...
                            //escrevem de um jeito na RAS e no cadastro de veiculo de outro Â¬Â¬
                            String nome = resultVitima.getString("nome_vitima");
                            if (veiculo.getCondutor().equalsIgnoreCase(nome)) {
                                tipoVeiculo = veiculo.getTipoVeiculo();
                                break;
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
                    insertVitima += ", \'" + ids.getId_Ras() + "\'";
                    insertVitima += ", \'Não informado\'";
                    insertVitima += ", \'" + tipoVeiculo + "\'";

                    //Pegando Sinais clinicos...
                    String sinaisClinicos = resultVitima.getString("array_sinaisclinicos");
                    if (!sinaisClinicos.equals("")) {
                        String[] arraySinaisClinicos = sinaisClinicos.split(",");
                        for (int i = 0; i < arraySinaisClinicos.length; i++) {
//                            System.out.println("INSERT INTO tiposinalclinico (descricao) VALUES(\'" + arraySinaisClinicos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                           LocalCon.executeSQL("INSERT INTO tiposinalclinico (descricao) VALUES(\'" + arraySinaisClinicos[i].replaceAll("['<>\\|/]", " ") + "\');");
                        }
                    }

                    insertVitima += ", \'" + sinaisClinicos.replaceAll("['<>\\|/]", " ") + "\'";

                    //Pegando procedimentos...
                    String procedimentos = resultVitima.getString("array_procedimentos");
                    if (!procedimentos.equals("")) {
                        String[] arrayProcedimentos = procedimentos.split(", ");
                        for (int i = 0; i < arrayProcedimentos.length; i++) {
//                            System.out.println("INSERT INTO tipoprocedimento (descricao) VALUES(\'" + arrayProcedimentos[i].replaceAll("['<>\\|/]", " ") + "\');");
//                            LocalCon.executeSQL("INSERT INTO tipoprocedimento (descricao) VALUES(\'" + arrayProcedimentos[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");

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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");

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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
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
//                            LocalCon.executeSQL("INSERT INTO formularios.tipolesao(descricao) VALUES (\'" + vet[i].replaceAll("['<>\\|/]", " ") + "\');");
                        }
                    }
                    //mid
                    insertVitima += ", \'" + lesao.replaceAll("['<>\\|/]", " ") + "\') returning id;";

//                    System.out.println(insertVitima);
                    ResultSet rsInsert = LocalCon.executeQuery(insertVitima);
                    rsInsert.next();
                    Long id_vitimaRAS = rsInsert.getLong("id");

                    /*Verifica (usando o nome) se a pessoa jÃ¡ estÃ¡ cadastrada. Caso esteja, um novo registro vinculado Ã© criado */
//                    System.out.println("SELECT id, nome from pessoa where nome=\'" + vitimaRAS.getNomeVitima().replaceAll("['<>\\|/]", " ") + "\'");
                    ResultSet pessoasResult = LocalCon.executeQuery("SELECT id, nome from pessoa where nome=\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\'");
                    Long id_pessoa;

                    if (pessoasResult.next()) {
                        id_pessoa = pessoasResult.getLong("id");
                    } else {
                        //NÃ£o encontrou pessoa semelhante na base de dados
                        String insertPessoa = preparaConsultaPessoa();

                        insertPessoa += "(\'" + resultVitima.getString("nome_vitima").replaceAll("['<>\\|/]", " ") + "\'";
                        insertPessoa += ", \'\') returning id;";

//                        System.out.println(insertPessoa);
                        ResultSet pessoaInserted = LocalCon.executeQuery(insertPessoa);
                        pessoaInserted.next();
                        id_pessoa = pessoaInserted.getLong("id");
                    }

                    Long id_condutor = null;

                    for (TempVeiculo veiculo : listVeiculos) {
                        if (veiculo.getCondutor().equals(resultVitima.getString("nome_vitima"))) {
//                            System.out.println("INSERT INTO veiculo(cidade, placa) VALUES (\'" + veiculo.getMunicipio().replaceAll("['<>\\|/]", " ") + "\', \'" + veiculo.getPlaca().replaceAll("['<>\\|/]", " ") + "\') returning id;");
                            ResultSet resultV = LocalCon.executeQuery("INSERT INTO veiculo(cidade, placa) VALUES (\'" + veiculo.getMunicipio().replaceAll("['<>\\|/]", " ") + "\', \'" + veiculo.getPlaca().replaceAll("['<>\\|/]", " ") + "\') returning id;");
                            resultV.next();
                            Long id_veiculo = resultV.getLong("id");
//                            System.out.println("INSERT INTO condutor(veiculo_id, sintomasembriaguez, solicitacaoexame, estadocondutor, categoriahabilitacao, situacaohabilitacao, tipohabilitacao) VALUES (\'" + id_veiculo + "\', false, false, \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\') returning id;");
                            ResultSet resultCondutor = LocalCon.executeQuery("INSERT INTO condutor(veiculo_id, sintomasembriaguez, solicitacaoexame, estadocondutor, categoriahabilitacao, situacaohabilitacao, tipohabilitacao) VALUES (\'" + id_veiculo + "\', false, false, \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\') returning id;");
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
                    insertEnvolvido += ", \'" + ids.getId_ocorrencia() + "\'";
                    insertEnvolvido += ", \'Não informado\'";
                    insertEnvolvido += ", \'" + tipoVeiculo + "\'";
                    insertEnvolvido += ", \'" + new TipoProfissao().getDescricao() + "\'";
                    insertEnvolvido += ", \'" + EnumEscolaridade.NAO_INFORMADO + "\'";
                    insertEnvolvido += ", \'" + EnumSituacaoCivil.NAO_INFORMADO + "\'";
                    insertEnvolvido += ", \'" + false + "\'";
                    insertEnvolvido += ", " + id_condutor;
                    insertEnvolvido += ", \'" + id_vitimaRAS + "\');";

//                    System.out.println(insertEnvolvido);
                    LocalCon.executeSQL(insertEnvolvido);
                }
            }
            for (TempVeiculo veiculo : listVeiculos) {
                /* Este caso trata-se de um envolvido que nÃ£o o teve atendimento mÃ©dico - logo, nÃ£o teve RAS. 
                             
                 * Primeiro passo: Inserir uma nova pessoa
                 * Segundo passo: Inserir tipo de veiculo
                 * Terceiro passo: Inserir novo envolvido
                 * Quarto passo: Inserir veiculo/condutor
                 */
                String query = "SELECT id, nome from pessoa where nome=\'" + veiculo.getCondutor().replaceAll("['<>\\|/]", " ") + "\'";
                ResultSet pessoasResult = LocalCon.executeQuery(query);
                Long id_pessoa;
                if (pessoasResult.next()) {
                    id_pessoa = pessoasResult.getLong("id");
                } else {
                    //NÃ£o encontrou pessoa semelhante na base de dados
                    String insertPessoa = preparaConsultaPessoa();

                    insertPessoa += "(\'" + veiculo.getCondutor().replaceAll("['<>\\|/]", " ") + "\'";
                    insertPessoa += ", \'\') returning id;";

//                    System.out.println(insertPessoa);
                    ResultSet pessoaInserted = LocalCon.executeQuery(insertPessoa);
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
                insertEnvolvido += ", \'" + ids.getId_ocorrencia() + "\'";
                insertEnvolvido += ", \'Não informado\'";
                insertEnvolvido += ", \'" + new TipoProfissao().getDescricao() + "\'";
                insertEnvolvido += ", \'" + EnumEscolaridade.NAO_INFORMADO + "\'";
                insertEnvolvido += ", \'" + EnumSituacaoCivil.NAO_INFORMADO + "\'";
                insertEnvolvido += ", \'" + false + "\'";
                insertEnvolvido += ", \'" + veiculo.getTipoVeiculo() + "\') returning id;";

//                System.out.println(insertEnvolvido);
                ResultSet envolvidoInserted = LocalCon.executeQuery(insertEnvolvido);
                envolvidoInserted.next();
                Long id_envolvido = envolvidoInserted.getLong("id");

                query = "INSERT INTO veiculo(cidade, placa) VALUES (\'" + veiculo.getMunicipio().replaceAll("['<>\\|/]", " ") + "\', \'" + veiculo.getPlaca().replaceAll("['<>\\|/]", " ") + "\') returning id;";
//                System.out.println(query);
                ResultSet resultV = LocalCon.executeQuery(query);
                resultV.next();
                Long id_veiculo = resultV.getLong("id");
                query = "INSERT INTO condutor(id, veiculo_id, sintomasembriaguez, solicitacaoexame, estadocondutor, categoriahabilitacao, situacaohabilitacao, tipohabilitacao) VALUES (\'" + id_envolvido + "\', \'" + id_veiculo + "\', false, false, \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\', \'NAO_INFORMADO\');";
//                System.out.println(query);
                LocalCon.executeSQL(query);
            }
            System.gc();
        }
        System.out.println("Total de registros: " + totalRegistrosRGOLidos);
        System.out.println("Total de registros georreferenciados: " + totalRegistrosGeo);
    }

    public boolean update(long num_ocor) throws Exception {
//        //Pega o nÃºmero da ocorrÃªncia nos bombeiros para acidentes do tipo trÃ¢nsito...
//        String sql = "SELECT * FROM acidente WHERE num_ocor=" + num_ocor;
//        ResultSet rs = RemoteCon.executeQuery(sql);
//        if (rs.next()) {
//            if (existeOcorrencia(num_ocor)) {
//                sql = "UPDATE acidente SET tipoacidente_id=" + rs.getShort("tipoacidente_id") + ", datahora='" + rs.getTimestamp("datahora") + "', ts='" + task.getTs() + "' WHERE num_ocor=" + num_ocor;
//                return LocalCon.executeSQL(sql);
//            }
//        }
        return true;
    }

    private static Date processDate(String timestamp, boolean date) {
        String[] data = timestamp.split(" ");

        if (date) {
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dateFormated = sdf.parse(data[0]);
                return dateFormated;

            } catch (ParseException ex) {
                Logger.getLogger(Atualiza_sigetrans.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Date dateFormated = sdf.parse(data[1]);
                //System.out.println(dateFormated);
                return dateFormated;

            } catch (ParseException ex) {
                Logger.getLogger(Atualiza_sigetrans.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static String preparaConsultaRGO() {

        return "select cod_rgo, datahora, endereco_geral.log_nome, endereco_cruzamento.log_nome as cruzamento, referencia, descr_ocorrencia, ocorrencia, nome as causa, vitima as possui_vitima "
                + "from (select cod_fracao, cod_rgo, datahora, tipoocorrencia, descr_ocorrencia, endereco, esquina, referencia, cod_causa, vitima from rgo where cod_municipio=207 and datahora between '2014-07-04' and '2014-07-04') as tab_ocorrencia "
                + "inner join (select cod_ocorrencia, ocorrencia from ts_ocorrencia where quadroccb='Acidentes de trânsito') as tab_tp_ocorrencia on (tipoocorrencia=cod_ocorrencia) "
                + "inner join (select cod_causa, nome from ts_causa) as tab_causa using(cod_causa) "
                + "inner join (select id_logradouro, log_nome log_no_sem_acento, log_nome from log_logradouro) as endereco_geral on (endereco_geral.id_logradouro=endereco) "
                + "left outer join (select id_logradouro, log_nome log_no_sem_acento, log_nome from log_logradouro) as endereco_cruzamento on (endereco_cruzamento.id_logradouro=esquina)";
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

    private static String preparaConsultaPessoa() {
        return "INSERT INTO pessoa("
                + "nome, "
                + "rua)"
                + " VALUES ";
    }

    private static Endereco processaEndereco(String rua, String cruzamento) {
//        boolean consultaGeo = true;
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
            System.out.println("consultando rua...");
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
        return end;
    }

    public static boolean verificaEnvolvidoAtivo(Date dataOcorrencia) {
        Date periodoOMS = FuncoesEstaticas.decrementDate(new Date(), 30);
        return periodoOMS.compareTo(dataOcorrencia) >= 0 ? false : true;
    }
}
