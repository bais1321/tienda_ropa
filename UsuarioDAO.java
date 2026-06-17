package dao;

import modelo.Usuario;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones de la tabla usuarios.
 */
public class UsuarioDAO {

    // ─── Registrar nuevo usuario ──────────────────────────
    public boolean registrar(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, apellido, telefono, email, password, es_admin, primera_compra_usada, fecha_registro) "
                   + "VALUES (?, ?, ?, ?, ?, 0, 0, NOW())";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getTelefono());
            ps.setString(4, u.getEmail());
            ps.setString(5, PasswordUtil.hashear(u.getPassword()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.registrar: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Registrar usuario desde datos de invitado ────────
    // Se usa cuando el invitado crea cuenta post-compra.
    public boolean registrarDesdeInvitado(String nombre, String apellido,
            String telefono, String email, String passwordPlano) {
        Usuario u = new Usuario(nombre, apellido, telefono, email, passwordPlano);
        return registrar(u);
    }

    // ─── Login: verificar email + password ────────────────
    public Usuario login(String email, String passwordPlano) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashGuardado = rs.getString("password");
                if (PasswordUtil.verificar(passwordPlano, hashGuardado)) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.login: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Buscar por ID ────────────────────────────────────
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.buscarPorId: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Buscar por email ─────────────────────────────────
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.buscarPorEmail: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Verificar si email ya existe ────────────────────
    public boolean emailExiste(String email) {
        String sql = "SELECT id FROM usuarios WHERE email = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.emailExiste: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Listar todos los clientes (no admins) ────────────
    public List<Usuario> listarClientes() {
        String sql = "SELECT * FROM usuarios WHERE es_admin = 0 ORDER BY id DESC";
        List<Usuario> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.listarClientes: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    // ─── Marcar primera compra como usada ─────────────────
    public boolean marcarPrimeraCompraUsada(int usuarioId) {
        String sql = "UPDATE usuarios SET primera_compra_usada = 1 WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.marcarPrimeraCompraUsada: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Actualizar datos del usuario ─────────────────────
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE usuarios SET nombre=?, apellido=?, telefono=? WHERE id=?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getTelefono());
            ps.setInt(4, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.actualizar: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Cambiar contraseña ───────────────────────────────
    public boolean cambiarPassword(int usuarioId, String nuevaPasswordPlano) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, PasswordUtil.hashear(nuevaPasswordPlano));
            ps.setInt(2, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UsuarioDAO.cambiarPassword: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Mapear ResultSet → Usuario ───────────────────────
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setTelefono(rs.getString("telefono"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setEsAdmin(rs.getBoolean("es_admin"));
        u.setPrimeraCompraUsada(rs.getBoolean("primera_compra_usada"));
        u.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return u;
    }
}
