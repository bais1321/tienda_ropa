package util;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
public class DBConnection {
 
    private static final String DB_HOST   = "localhost";
    private static final String DB_PORT   = "3308";
    private static final String DB_NAME   = "tienda_en_linea";
    private static final String DB_USER   = "byron";
    private static final String DB_PASS   = "";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
 
    private static final String URL = "jdbc:mysql://"
            + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false"
            + "&serverTimezone=America/Guatemala"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8"
            + "&autoReconnect=true";
 
    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "ERROR: Driver MySQL no encontrado. Agrega mysql-connector-j a WEB-INF/lib/\n"
                + e.getMessage());
        }
    }
 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }
 
    public static void close(Connection con) {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
 
    public static boolean testConnection() {
        Connection con = null;
        try {
            con = getConnection();
            return true;
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        } finally {
            close(con);
        }
    }
}