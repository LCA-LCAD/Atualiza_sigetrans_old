package atualiza_sigetrans;

import atualiza_sigetrans.domain.Ocorrencia;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Henrique
 */
public class FuncoesEstaticas {

    private static Logger log = Logger.getLogger(Ocorrencia.class);
    public static String[] REPLACES = {"a", "e", "i", "o", "u", "c"};
    public static Pattern[] PATTERNS = null;

    private static void compilePatterns() {
        PATTERNS = new Pattern[REPLACES.length];
        PATTERNS[0] = Pattern.compile("[âãáàä]", Pattern.CASE_INSENSITIVE);
        PATTERNS[1] = Pattern.compile("[éèêë]", Pattern.CASE_INSENSITIVE);
        PATTERNS[2] = Pattern.compile("[íìîï]", Pattern.CASE_INSENSITIVE);
        PATTERNS[3] = Pattern.compile("[óòôõö]", Pattern.CASE_INSENSITIVE);
        PATTERNS[4] = Pattern.compile("[úùûü]", Pattern.CASE_INSENSITIVE);
        PATTERNS[5] = Pattern.compile("[ç]", Pattern.CASE_INSENSITIVE);
    }

    public static String replaceSpecial(String text) {
        if (PATTERNS == null) {
            compilePatterns();
        }
        String result = text;
        for (int i = 0; i < PATTERNS.length; i++) {
            Matcher matcher = PATTERNS[i].matcher(result);
            result = matcher.replaceAll(REPLACES[i]);
        }
        return result;
    }

    public static String removeAccents(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str;
    }
    
    public static String removeCaracteresEspecias(String str) {
        PATTERNS = new Pattern[REPLACES.length];
        return str;
    }

    public static Integer calculateAge(Date atual, Date nascimento) {
        Calendar dataNascimento = Calendar.getInstance();
        dataNascimento.setTime(nascimento);
        Calendar dataAtual = Calendar.getInstance();
        dataAtual.setTime(atual);

        int diferencaMes = dataAtual.get(Calendar.MONTH) - dataNascimento.get(Calendar.MONTH);
        int diferencaDia = dataAtual.get(Calendar.DAY_OF_MONTH) - dataNascimento.get(Calendar.DAY_OF_MONTH);
        int idade = (dataAtual.get(Calendar.YEAR) - dataNascimento.get(Calendar.YEAR));

        if (diferencaMes < 0 || (diferencaMes == 0 && diferencaDia < 0)) {
            idade--;
        }
        return new Integer(idade);
    }

    public static String prepareStatement(String nome) {
        log.info("String recebida [" + nome + "]");
        //Prepara um nome para ser consultado  no banco. 
        String[] replaces1 = {"_"};
        Pattern[] patterns1 = new Pattern[replaces1.length];
        //
        //
        patterns1[0] = Pattern.compile("[aâãáàäeéèêëiíìîïoóòôõöuúùûü]", Pattern.CASE_INSENSITIVE); //retirar acentos
        //Pegar casos Luiz = Luis, Daiane = Dayane, Uillian = Willian e outros.
//        patterns1[1] = Pattern.compile("[gjszuwviy]", Pattern.CASE_INSENSITIVE);
//        for (int i = 0; i < patterns1.length; i++) {
        Matcher matcher = patterns1[0].matcher(nome);
        //Substituindo
        nome = matcher.replaceAll(replaces1[0]);
//        }
        nome = nome.replaceFirst(" ", "%");
        nome+="%";
        
        log.info("retornando [" + nome + "]");
        return nome;
    }

    public static Date decrementDate(Date data, int days) {
        Calendar dataResult = Calendar.getInstance();
        dataResult.setTime(data);
//        if((dataResult.get(Calendar.DAY_OF_MONTH) - days) > 0){
        dataResult.add(Calendar.DAY_OF_MONTH, -days);
//        } else {
        log.info("Data: " + dataResult.getTime().toString());
//        }
        return dataResult.getTime();
    }

    public static int compareToDate(Date date1, Date date2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(date1);
        c2.setTime(date2);
        long dif = c2.getTimeInMillis() - c1.getTimeInMillis();
        int tempDay = 1000 * 60 * 60 * 24;
        return Math.round((dif / tempDay));
    }

    public static List<Date> searchDayOfWeek(Date date1, Date date2, List<String> dayOfWeek) {
        List<Date> listDayOfWeek = new ArrayList<>();
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(date1);
        while (compareToDate(calendario.getTime(), date2) > 0) {

            for (int i = 0; i < dayOfWeek.size(); i++) {
                if (dayOfWeek.get(i).equals("Domingo")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 1) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
                if (dayOfWeek.get(i).equals("Segunda-Feira")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 2) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
                if (dayOfWeek.get(i).equals("Terça-Feira")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 3) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
                if (dayOfWeek.get(i).equals("Quarta-Feira")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 4) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
                if (dayOfWeek.get(i).equals("Quinta-Feira")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 5) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
                if (dayOfWeek.get(i).equals("Sexta-Feira")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 6) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
                if (dayOfWeek.get(i).equals("Sábado")) {
                    if (calendario.get(Calendar.DAY_OF_WEEK) == 7) {
                        listDayOfWeek.add(calendario.getTime());
                        break;
                    }
                }
            }
            calendario.setTime(decrementDate(calendario.getTime(), -1));
        }
        return listDayOfWeek;
    }

    public static DualListModel<Date> converterTime(List<String> time) throws ParseException {

        List<Date> timeI = new ArrayList<>();
        List<Date> timeF = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (int i = 0; i < time.size(); i++) {
                String[] vetor = time.get(i).split("às");
                timeI.add(sdf.parse(vetor[0]));
                timeF.add(sdf.parse(vetor[1]));
        }

        return new DualListModel<>(timeI, timeF);
    }

    public static boolean validateDate(Date date1) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(date1);
        c2.setTime(new Date());
        return c2.getTimeInMillis() - c1.getTimeInMillis() >= 0 ? true : false;
    }
}
