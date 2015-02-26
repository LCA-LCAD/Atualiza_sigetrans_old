package atualiza_sigetrans;

import atualiza_sigetrans.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Lucas
 */
public class RemoteCon {

    public static Connection con = null;
    private static Statement stm = null;
    protected static String host, db, user, passwd;
    protected static int port;
    public static long timeout = 20;
    private static boolean error = false;

    public static void set(String HOST, int PORT, String DB, String USER, String PASSWD) {

        host = HOST;
        port = PORT;
        db = DB;
        user = USER;
        passwd = PASSWD;

    }

    public static boolean connect() {
        try {
            Thread.sleep(timeout);
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "", user, passwd);
            stm = con.createStatement();
            error = false;
            return true;
        } catch (InterruptedException | ClassNotFoundException | SQLException ex) {
            Thread.currentThread().interrupt();
            if (!error) {
                Logger.send(ex, "REMOTE_HOST");
                error = true;
            }
            return false;
        }
    }

    public static ResultSet executeQuery(String sql) {
        ResultSet rs = null;                          
        while (true) {
            //Tenta gravar as alterações remotamente
            //con.setAutoCommit(false);
            try {
                if (checkCon()) {
                    rs = stm.executeQuery(sql);
                    error = false;
                    // con.commit();
                    // con.setAutoCommit(true);
                    return rs;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (!error) {
                    Logger.send(ex, "REMOTE_HOST");
                    error = true;
                }
                checkCon();
            }
        }
    }

    public static boolean executeSQL(String sql) {
        while (true) {
            //Tenta gravar as alterações remotamente
            //con.setAutoCommit(false);
            try {
                if (checkCon()) {
                    stm.execute(sql);
                    // con.commit();
                    // con.setAutoCommit(true);
                    error = false;
                    return true;
                }
            } catch (Exception ex) {
                //con.rollback();
                //System.out.println("2");
                if (!error) {
                    Logger.send(ex, "REMOTE_HOST");
                    error = true;
                }
                checkCon();
            }
        }
    }

    public static boolean close() {
        try {
            stm.close();
            con.close();
            return true;
        } catch (Exception ex) {
            Logger.send(ex, "REMOTE_HOST");
            return false;
        }
    }

    public static boolean checkCon() {
        while (true) {
            try {
                return stm.executeQuery("SELECT 1").next();
            } catch (Exception ex) {
                connect();
            }
        }
    }
}
