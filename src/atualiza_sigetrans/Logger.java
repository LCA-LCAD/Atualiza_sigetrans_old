package atualiza_sigetrans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;

/**
 *
 * @author Lucas
 */
public class Logger {

    private static FileOutputStream fos = null;
    private static File file = null;
    private static Writer writer;
    static PrintWriter printWriter;
    public static long fileLimit = 5000000;
    public static boolean store = true;
    public static boolean allErrors = true;

    private static void createNew() throws Exception {

        file = new File("log.txt");
        fos = new FileOutputStream(file, allErrors);
        writer = new StringWriter();
        printWriter = new PrintWriter(writer);

    }

    private static void write(Exception ex, String host) {
        String log1 = "", log2 = "", aux = "";
        try {
            ex.printStackTrace(printWriter);
            log1 = writer.toString();

            if (host.length() > 0) {
                host = host + ",";
            }

            log2 += host + (new Timestamp(System.currentTimeMillis()).toString()) + ": " + System.getProperty("line.separator");
            aux = log2;

            if (file.length() > 0) {
                log2 = System.getProperty("line.separator") + log2;
            }

            byte[] bytes = (log2 + log1).getBytes();

            if (file.length() + bytes.length > fileLimit) {

                fos.close();
                writer.close();
                printWriter.close();

                file.delete();
                createNew();

                log2 = aux;
            }

            fos.write((log2 + log1).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(Exception ex, String host) {
        if (store) {
            try {
                if (file != null) {
                    write(ex, host);
                } else {
                    createNew();
                    write(ex, host);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ex.printStackTrace();
        }
    }
}
