package dao;

import modelo.ImagenProducto;
import modelo.Producto;
import modelo.TallaProducto;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones de productos, tallas e imágenes.
 */
public class ProductoDAO {

    // ══════════════════════════════════════════════════════
    //  PRODUCTOS
    // ══════════════════════════════════════════════════════

    // ─── Listar productos activos con imagen principal y stock total ──
    public List<Producto> listarActivos() {
        return listarConFiltros(null, null, null, false);
    }

    // ─── Listar destacados (para el home) ─────────────────
    public List<Producto> listarDestacados() {
        String sql = "SELECT * FROM vista_productos WHERE es_destacado = 1 AND estado = 'activo' LIMIT 8";
        return ejecutarListaProductos(sql);
    }

    // ─── Listar con filtros (catálogo con búsqueda) ───────
    public List<Producto> listarConFiltros(String categoria, String subcategoria,
                                            String busqueda, boolean soloConDescuento) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM vista_productos WHERE estado != 'inactivo'");
        List<Object> params = new ArrayList<>();

        if (categoria != null && !categoria.isEmpty()) {
            sql.append(" AND categoria = ?");
            params.add(categoria);
        }
        if (subcategoria != null && !subcategoria.isEmpty()) {
            sql.append(" AND subcategoria = ?");
            params.add(subcategoria);
        }
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append(" AND (nombre LIKE ? OR descripcion LIKE ?)");
            params.add("%" + busqueda.trim() + "%");
            params.add("%" + busqueda.trim() + "%");
        }
        if (soloConDescuento) {
            sql.append(" AND descuento > 0");
        }
        sql.append(" ORDER BY es_destacado DESC, id DESC");

        List<Producto> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearVista(rs));
        } catch (SQLException e) {
            System.err.println("ProductoDAO.listarConFiltros: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    // ─── Buscar producto por ID con tallas e imágenes ─────
    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM vista_productos WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Producto p = mapearVista(rs);
                p.setTallas(listarTallas(id, con));
                p.setImagenes(listarImagenes(id, con));
                return p;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.buscarPorId: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Productos relacionados (misma categoría + subcategoría) ─
    public List<Producto> listarRelacionados(int productoId, String categoria,
                                              String subcategoria, int limite) {
        String sql = "SELECT * FROM vista_productos "
                   + "WHERE estado = 'activo' AND id != ? "
                   + "AND categoria = ? AND subcategoria = ? "
                   + "ORDER BY RAND() LIMIT ?";
        List<Producto> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productoId);
            ps.setString(2, categoria);
            ps.setString(3, subcategoria);
            ps.setInt(4, limite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearVista(rs));
        } catch (SQLException e) {
            System.err.println("ProductoDAO.listarRelacionados: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    // ─── Listar todos (admin — incluye inactivos) ─────────
    public List<Producto> listarTodos() {
        String sql = "SELECT * FROM vista_productos ORDER BY id DESC";
        return ejecutarListaProductos(sql);
    }

    // ─── Listar con stock bajo para dashboard admin ───────
    public List<Producto> listarStockBajo() {
        String sql = "SELECT DISTINCT p.* FROM vista_stock_bajo s "
                   + "JOIN vista_productos p ON p.id = s.producto_id";
        return ejecutarListaProductos(sql);
    }

    // ─── Crear producto (sin tallas ni imágenes aún) ──────
    public int crear(Producto p) {
        String sql = "INSERT INTO productos (nombre, descripcion, precio_original, descuento, "
                   + "categoria, subcategoria, es_destacado, estado) VALUES (?,?,?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecioOriginal());
            ps.setInt(4, p.getDescuento());
            ps.setString(5, p.getCategoria());
            ps.setString(6, p.getSubcategoria());
            ps.setBoolean(7, p.isEsDestacado());
            ps.setString(8, p.getEstado() != null ? p.getEstado() : "activo");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.crear: " + e.getMessage());
            return -1;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Actualizar producto ──────────────────────────────
    public boolean actualizar(Producto p) {
        String sql = "UPDATE productos SET nombre=?, descripcion=?, precio_original=?, "
                   + "descuento=?, categoria=?, subcategoria=?, es_destacado=?, estado=? "
                   + "WHERE id=?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecioOriginal());
            ps.setInt(4, p.getDescuento());
            ps.setString(5, p.getCategoria());
            ps.setString(6, p.getSubcategoria());
            ps.setBoolean(7, p.isEsDestacado());
            ps.setString(8, p.getEstado());
            ps.setInt(9, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.actualizar: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Cambiar estado del producto ──────────────────────
    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE productos SET estado = ? WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.cambiarEstado: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Recalcular estado según stock total ──────────────
    // Llamar después de cada cambio de stock
    public void recalcularEstado(int productoId) {
        String sql = "UPDATE productos p "
                   + "SET p.estado = CASE "
                   + "  WHEN (SELECT COALESCE(SUM(stock),0) FROM tallas_producto WHERE producto_id = p.id) = 0 "
                   + "    THEN 'agotado' "
                   + "  WHEN p.estado = 'agotado' THEN 'activo' "
                   + "  ELSE p.estado END "
                   + "WHERE p.id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ProductoDAO.recalcularEstado: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
    }

    // ══════════════════════════════════════════════════════
    //  TALLAS
    // ══════════════════════════════════════════════════════

    public List<TallaProducto> listarTallasPorProducto(int productoId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            return listarTallas(productoId, con);
        } catch (SQLException e) {
            System.err.println("ProductoDAO.listarTallasPorProducto: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            DBConnection.close(con);
        }
    }

    public TallaProducto buscarTallaPorId(int tallaId) {
        String sql = "SELECT * FROM tallas_producto WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, tallaId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapearTalla(rs) : null;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.buscarTallaPorId: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    public boolean agregarTalla(TallaProducto t) {
        String sql = "INSERT INTO tallas_producto (producto_id, talla, stock) VALUES (?,?,?)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, t.getProductoId());
            ps.setString(2, t.getTalla());
            ps.setInt(3, t.getStock());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.agregarTalla: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    public boolean actualizarStockTalla(int tallaId, int nuevoStock) {
        String sql = "UPDATE tallas_producto SET stock = ? WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, nuevoStock);
            ps.setInt(2, tallaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.actualizarStockTalla: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    public boolean eliminarTalla(int tallaId) {
        String sql = "DELETE FROM tallas_producto WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, tallaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.eliminarTalla: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    // ─── Descontar stock al generar pedido ────────────────
    // Usa FOR UPDATE para evitar condiciones de carrera
    public boolean descontarStock(int tallaId, int cantidad, Connection con) throws SQLException {
        String sqlCheck = "SELECT stock FROM tallas_producto WHERE id = ? FOR UPDATE";
        String sqlUpdate = "UPDATE tallas_producto SET stock = stock - ? WHERE id = ? AND stock >= ?";
        PreparedStatement psCheck = con.prepareStatement(sqlCheck);
        psCheck.setInt(1, tallaId);
        ResultSet rs = psCheck.executeQuery();
        if (!rs.next() || rs.getInt("stock") < cantidad) {
            return false; // Stock insuficiente
        }
        PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
        psUpdate.setInt(1, cantidad);
        psUpdate.setInt(2, tallaId);
        psUpdate.setInt(3, cantidad);
        return psUpdate.executeUpdate() > 0;
    }

    // ─── Restaurar stock al cancelar pedido ───────────────
    public boolean restaurarStock(int tallaId, int cantidad, Connection con) throws SQLException {
        String sql = "UPDATE tallas_producto SET stock = stock + ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, cantidad);
        ps.setInt(2, tallaId);
        return ps.executeUpdate() > 0;
    }

    // ══════════════════════════════════════════════════════
    //  IMÁGENES
    // ══════════════════════════════════════════════════════

    public List<ImagenProducto> listarImagenesPorProducto(int productoId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            return listarImagenes(productoId, con);
        } catch (SQLException e) {
            System.err.println("ProductoDAO.listarImagenesPorProducto: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            DBConnection.close(con);
        }
    }

    public boolean agregarImagen(ImagenProducto img) {
        String sql = "INSERT INTO imagenes_producto (producto_id, ruta_imagen, orden) VALUES (?,?,?)";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, img.getProductoId());
            ps.setString(2, img.getRutaImagen());
            ps.setInt(3, img.getOrden());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.agregarImagen: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    public boolean eliminarImagen(int imagenId) {
        String sql = "DELETE FROM imagenes_producto WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, imagenId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductoDAO.eliminarImagen: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(con);
        }
    }

    public int contarImagenes(int productoId) {
        String sql = "SELECT COUNT(*) FROM imagenes_producto WHERE producto_id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productoId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        } finally {
            DBConnection.close(con);
        }
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════

    private List<Producto> ejecutarListaProductos(String sql) {
        List<Producto> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            while (rs.next()) lista.add(mapearVista(rs));
        } catch (SQLException e) {
            System.err.println("ProductoDAO.ejecutarListaProductos: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    private List<TallaProducto> listarTallas(int productoId, Connection con) throws SQLException {
        String sql = "SELECT * FROM tallas_producto WHERE producto_id = ? ORDER BY talla";
        List<TallaProducto> lista = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, productoId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) lista.add(mapearTalla(rs));
        return lista;
    }

    private List<ImagenProducto> listarImagenes(int productoId, Connection con) throws SQLException {
        String sql = "SELECT * FROM imagenes_producto WHERE producto_id = ? ORDER BY orden";
        List<ImagenProducto> lista = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, productoId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) lista.add(mapearImagen(rs));
        return lista;
    }

    // ─── Mapeadores ───────────────────────────────────────

    private Producto mapearVista(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioOriginal(rs.getBigDecimal("precio_original"));
        p.setDescuento(rs.getInt("descuento"));
        p.setCategoria(rs.getString("categoria"));
        p.setSubcategoria(rs.getString("subcategoria"));
        p.setEsDestacado(rs.getBoolean("es_destacado"));
        p.setEstado(rs.getString("estado"));
        p.setStockTotal(rs.getInt("stock_total"));
        // Imagen principal desde la vista
        String imgPrincipal = rs.getString("imagen_principal");
        if (imgPrincipal != null) {
            ImagenProducto img = new ImagenProducto();
            img.setRutaImagen(imgPrincipal);
            img.setOrden(0);
            List<ImagenProducto> imgs = new ArrayList<>();
            imgs.add(img);
            p.setImagenes(imgs);
        }
        return p;
    }

    private TallaProducto mapearTalla(ResultSet rs) throws SQLException {
        TallaProducto t = new TallaProducto();
        t.setId(rs.getInt("id"));
        t.setProductoId(rs.getInt("producto_id"));
        t.setTalla(rs.getString("talla"));
        t.setStock(rs.getInt("stock"));
        return t;
    }

    private ImagenProducto mapearImagen(ResultSet rs) throws SQLException {
        ImagenProducto img = new ImagenProducto();
        img.setId(rs.getInt("id"));
        img.setProductoId(rs.getInt("producto_id"));
        img.setRutaImagen(rs.getString("ruta_imagen"));
        img.setOrden(rs.getInt("orden"));
        return img;
    }
}
