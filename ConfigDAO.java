package dao;

import modelo.ConfigTienda;
import util.DBConnection;

import java.sql.*;

/**
 * DAO para la configuración de la tienda (siempre 1 fila).
 */
public class ConfigDAO {

    private static ConfigTienda cache = null; // Cache en memoria

    // ─── Obtener configuración ────────────────────────────
    public ConfigTienda obtener() {
        if (cache != null) return cache; // Usar cache si existe
        String sql = "SELECT * FROM configuracion_tienda WHERE id = 1";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            if (rs.next()) {
                cache = mapear(rs);
                return cache;
            }
            return new ConfigTienda(); // Config vacía si no existe
        } catch (SQLException e) {
            System.err.println("ConfigDAO.obtener: " + e.getMessage());
            return new ConfigTienda();
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Guardar / actualizar configuración ──────────────
    public boolean guardar(ConfigTienda c) {
        String sql = "INSERT INTO configuracion_tienda "
                   + "(id, nombre_tienda, url_logo, numero_whatsapp, datos_bancarios, "
                   + "costo_envio_domicilio, minimo_domicilio, email_notificaciones, "
                   + "smtp_host, smtp_puerto, smtp_password) "
                   + "VALUES (1,?,?,?,?,?,?,?,?,?,?) "
                   + "ON DUPLICATE KEY UPDATE "
                   + "nombre_tienda=VALUES(nombre_tienda), url_logo=VALUES(url_logo), "
                   + "numero_whatsapp=VALUES(numero_whatsapp), datos_bancarios=VALUES(datos_bancarios), "
                   + "costo_envio_domicilio=VALUES(costo_envio_domicilio), "
                   + "minimo_domicilio=VALUES(minimo_domicilio), "
                   + "email_notificaciones=VALUES(email_notificaciones), "
                   + "smtp_host=VALUES(smtp_host), smtp_puerto=VALUES(smtp_puerto), "
                   + "smtp_password=VALUES(smtp_password)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, c.getNombreTienda());
            ps.setString(2, c.getUrlLogo());
            ps.setString(3, c.getNumeroWhatsapp());
            ps.setString(4, c.getDatosBancarios());
            ps.setBigDecimal(5, c.getCostoEnvioDomicilio());
            ps.setBigDecimal(6, c.getMinimoDomicilio());
            ps.setString(7, c.getEmailNotificaciones());
            ps.setString(8, c.getSmtpHost());
            ps.setInt(9, c.getSmtpPuerto());
            // No actualizar password si viene vacío
            if (c.getSmtpPassword() != null && !c.getSmtpPassword().isEmpty()) {
                ps.setString(10, c.getSmtpPassword());
            } else {
                ps.setNull(10, Types.VARCHAR);
            }
            boolean ok = ps.executeUpdate() > 0;
            if (ok) cache = null; // Invalidar cache
            return ok;
        } catch (SQLException e) {
            System.err.println("ConfigDAO.guardar: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    /** Invalida el cache (llamar después de actualizar desde el admin) */
    public void invalidarCache() { cache = null; }

    private ConfigTienda mapear(ResultSet rs) throws SQLException {
        ConfigTienda c = new ConfigTienda();
        c.setId(rs.getInt("id"));
        c.setNombreTienda(rs.getString("nombre_tienda"));
        c.setUrlLogo(rs.getString("url_logo"));
        c.setNumeroWhatsapp(rs.getString("numero_whatsapp"));
        c.setDatosBancarios(rs.getString("datos_bancarios"));
        c.setCostoEnvioDomicilio(rs.getBigDecimal("costo_envio_domicilio"));
        c.setMinimoDomicilio(rs.getBigDecimal("minimo_domicilio"));
        c.setEmailNotificaciones(rs.getString("email_notificaciones"));
        c.setSmtpHost(rs.getString("smtp_host"));
        c.setSmtpPuerto(rs.getInt("smtp_puerto"));
        c.setSmtpPassword(rs.getString("smtp_password"));
        return c;
    }
}
