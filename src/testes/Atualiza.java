/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import org.postgresql.util.PSQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
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
public class Atualiza {

    public static Connection Conexao = null;
    public static Connection con = null;
    public static Statement Comando = null;
    public static Statement stm = null;
    public static PreparedStatement sigetrans_ComandoSQL = null;
    public static boolean error;

    public static boolean Conecta_bombeiros() {
        try {
            Thread.sleep(20);
            Class.forName("com.mysql.jdbc.Driver");
            Conexao = DriverManager.getConnection("jdbc:mysql://201.41.143.66:6304/sysbmccb", "SIGETRANS", "cotrans193#");
            Conexao.setAutoCommit(false);
            Comando = Conexao.createStatement();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(Atualiza.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; 

    }

    public static ResultSet Consulta_bombeiros(String Consulta) {
        if (checkCon_bombeiros()){
            try {
                return Comando.executeQuery(Consulta);
            } catch (SQLException ex) {
                Logger.getLogger(Atualiza.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            System.out.println("Erro conect bombeiros.");
        }    
        return null;
    }

    public static boolean Conecta_sigetrans() {        
        try {
            Thread.sleep(20);
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sigetrans", "postgres", "postgres");
            stm = con.createStatement();
            error = false;
            return true;
        } catch (Exception ex) {
            if (!error) {
                atualiza_sigetrans.Logger.send(ex, "LOCAL_HOST");
                error = true;
            }
            return false;
        }
    }

    public static ResultSet Consulta_sigetrans(String Consulta) {
        if (checkCon_sigetrans()){
            try {
                return stm.executeQuery(Consulta);
            } catch (SQLException ex) {
                Logger.getLogger(Atualiza.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            System.out.println("Erro conect sigetrans.");
        }    
        return null;
    }
    
    public static int Update_sigetrans(String Consulta) {
        if (checkCon_sigetrans()){
            try {
                return stm.executeUpdate(Consulta);
            } catch (SQLException ex) {
                Logger.getLogger(Atualiza.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            System.out.println("Erro conect sigetrans.");
        }    
        return 0;
    }    

    public static boolean close_bombeiros() {

        try {
            stm.close();
            con.close();
            return true;

        } catch (Exception ex) {

            atualiza_sigetrans.Logger.send(ex, "LOCAL_HOST");

            return false;

        }

    }

    public static boolean close_sigetrans() {

        try {
            Comando.close();
            Conexao.close();
            return true;

        } catch (Exception ex) {

            atualiza_sigetrans.Logger.send(ex, "LOCAL_HOST");

            return false;

        }

    }    
    
    public static boolean executeSQL_bombeiros(String sql) {
        while (true) {
            //Tenta gravar as alterações remotamente
            //con.setAutoCommit(false);
            try {
                if (checkCon_bombeiros()) {
                    Comando.execute(sql);
                    // con.commit();
                    // con.setAutoCommit(true);
                    error = false;
                    return true;
                }
            } catch (Exception ex) {
                //con.rollback();
                //System.out.println("2");
                if (!error) {
                    atualiza_sigetrans.Logger.send(ex, "REMOTE_HOST");
                    error = true;
                }
                Conecta_bombeiros();
            }
        }
    }
    
    public static boolean executeSQL_sigetrans(String sql) {
        while (true) {
            //Tenta gravar as alterações remotamente
            //con.setAutoCommit(false);
            try {
                if (checkCon_sigetrans()) {
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
                    atualiza_sigetrans.Logger.send(ex, "REMOTE_HOST");
                    error = true;
                }
                Conecta_bombeiros();
            }
        }
    }   

    public static void Update_bombeiros(String updatesql) {
        try {
            Comando.executeUpdate(updatesql);
        } catch (SQLException ex) {
            Logger.getLogger(Atualiza.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void Update_sigetrans(PreparedStatement updatesql) {
        try {
            updatesql.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Atualiza.class.getName()).log(Level.SEVERE, null, ex);
        }
    }     
    
    public static boolean checkCon_bombeiros() {
        while (true) {
            try {
                return Comando.executeQuery("SELECT 1").next();
            } catch (Exception ex) {
                Conecta_bombeiros();
            }
        }
    }
    
    public static boolean checkCon_sigetrans() {
        while (true) {
            try {
                return stm.executeQuery("SELECT 1").next();
            } catch (Exception ex) {
                Conecta_sigetrans();
            }
        }
    }    
}
