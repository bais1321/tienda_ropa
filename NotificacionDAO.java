package dao;

import modelo.Notificacion;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para historial de notificaciones enviadas.
 */
public class NotificacionDAO {

    // ─── Guardar notificación ─────────────────────────────
    public boolean guardar(Notificacion n) {
        String sql = "INSERT INTO notificaciones "
                   + "(pedido_id, tipo, destinatario, asunto, mensaje, estado) "
                   + "VALUES (?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, n.getPedidoId());
            ps.setString(2, n.getTipo());
            ps.setString(3, n.getDestinatario());
            ps.setString(4, n.getAsunto());
            ps.setString(5, n.getMensaje());
            ps.setString(6, n.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NotificacionDAO.guardar: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Listar por pedido ────────────────────────────────
    public List<Notificacion> listarPorPedido(int pedidoId) {
        String sql = "SELECT * FROM notificaciones WHERE pedido_id = ? ORDER BY fecha DESC";
        List<Notificacion> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("NotificacionDAO.listarPorPedido: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setId(rs.getInt("id"));
        n.setPedidoId(rs.getInt("pedido_id"));
        n.setTipo(rs.getString("tipo"));
        n.setDestinatario(rs.getString("destinatario"));
        n.setAsunto(rs.getString("asunto"));
        n.setMensaje(rs.getString("mensaje"));
        n.setEstado(rs.getString("estado"));
        n.setFecha(rs.getTimestamp("fecha"));
        return n;
    }
}
