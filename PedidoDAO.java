package dao;

import modelo.DetallePedido;
import modelo.ItemCarrito;
import modelo.Pedido;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para pedidos y detalle_pedido.
 * La creación de pedido usa transacción manual para garantizar
 * atomicidad: si falla algo, se hace rollback completo.
 */
public class PedidoDAO {

    private ProductoDAO productoDAO = new ProductoDAO();

    // ══════════════════════════════════════════════════════
    //  CREAR PEDIDO (Transacción completa)
    // ══════════════════════════════════════════════════════

    /**
     * Genera un pedido completo:
     * 1. Valida stock de cada ítem
     * 2. Inserta en pedidos
     * 3. Inserta cada línea en detalle_pedido
     * 4. Descuenta stock de cada talla
     * 5. Recalcula estado de cada producto
     *
     * Si cualquier paso falla → ROLLBACK completo.
     *
     * @return El pedido creado con su ID y código, o null si hubo error
     */
    public Pedido crearPedido(Pedido pedido, List<ItemCarrito> items) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // INICIO TRANSACCIÓN

            // 1. Validar stock de TODOS los ítems antes de hacer nada
            for (ItemCarrito item : items) {
                String sqlStock = "SELECT stock FROM tallas_producto WHERE id = ? FOR UPDATE";
                PreparedStatement psStock = con.prepareStatement(sqlStock);
                psStock.setInt(1, item.getTallaId());
                ResultSet rsStock = psStock.executeQuery();
                if (!rsStock.next() || rsStock.getInt("stock") < item.getCantidad()) {
                    con.rollback();
                    throw new RuntimeException("Stock insuficiente para: "
                            + item.getNombreProducto() + " talla " + item.getTalla());
                }
            }

            // 2. Generar código único de pedido
            String prefijo = Pedido.PAGO_TRANSFERENCIA.equals(pedido.getTipoPago()) ? "T" : "E";
            String codigo = generarCodigoUnico(prefijo, con);
            pedido.setCodigoPedido(codigo);

            // 3. Insertar cabecera del pedido
            String sqlPedido =
                "INSERT INTO pedidos (codigo_pedido, usuario_id, nombre_cliente, apellido_cliente, "
                + "telefono_cliente, email_cliente, subtotal, descuento_primera_compra, "
                + "costo_envio, total, tipo_pago, tipo_entrega, direccion, estado) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'pendiente')";

            PreparedStatement psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            psPedido.setString(1, codigo);
            if (pedido.getUsuarioId() != null && pedido.getUsuarioId() > 0) {
                psPedido.setInt(2, pedido.getUsuarioId());
            } else {
                psPedido.setNull(2, Types.INTEGER);
            }
            psPedido.setString(3, pedido.getNombreCliente());
            psPedido.setString(4, pedido.getApellidoCliente());
            psPedido.setString(5, pedido.getTelefonoCliente());
            psPedido.setString(6, pedido.getEmailCliente());
            psPedido.setBigDecimal(7, pedido.getSubtotal());
            psPedido.setBigDecimal(8, pedido.getDescuentoPrimeraCompra());
            psPedido.setBigDecimal(9, pedido.getCostoEnvio());
            psPedido.setBigDecimal(10, pedido.getTotal());
            psPedido.setString(11, pedido.getTipoPago());
            psPedido.setString(12, pedido.getTipoEntrega());
            psPedido.setString(13, pedido.getDireccion());
            psPedido.executeUpdate();

            ResultSet keys = psPedido.getGeneratedKeys();
            if (!keys.next()) {
                con.rollback();
                return null;
            }
            int pedidoId = keys.getInt(1);
            pedido.setId(pedidoId);
            pedido.setEstado(Pedido.ESTADO_PENDIENTE);

            // 4. Insertar detalles + descontar stock
            String sqlDetalle =
                "INSERT INTO detalle_pedido (pedido_id, producto_id, talla_id, cantidad, "
                + "precio_unitario, descuento_aplicado) VALUES (?,?,?,?,?,?)";
            PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);

            String sqlDescontar =
                "UPDATE tallas_producto SET stock = stock - ? WHERE id = ? AND stock >= ?";
            PreparedStatement psDescontar = con.prepareStatement(sqlDescontar);

            for (ItemCarrito item : items) {
                // Insertar detalle
                psDetalle.setInt(1, pedidoId);
                psDetalle.setInt(2, item.getProductoId());
                psDetalle.setInt(3, item.getTallaId());
                psDetalle.setInt(4, item.getCantidad());
                psDetalle.setBigDecimal(5, item.getPrecioUnitario());
                psDetalle.setInt(6, item.getDescuentoAplicado());
                psDetalle.addBatch();

                // Descontar stock
                psDescontar.setInt(1, item.getCantidad());
                psDescontar.setInt(2, item.getTallaId());
                psDescontar.setInt(3, item.getCantidad());
                int filasActualizadas = psDescontar.executeUpdate();
                if (filasActualizadas == 0) {
                    con.rollback();
                    throw new RuntimeException("No se pudo descontar stock: " + item.getNombreProducto());
                }
            }
            psDetalle.executeBatch();

            con.commit(); // COMMIT ✅

            // 5. Recalcular estado de productos (fuera de la transacción crítica)
            for (ItemCarrito item : items) {
                productoDAO.recalcularEstado(item.getProductoId());
            }

            return pedido;

        } catch (Exception e) {
            System.err.println("PedidoDAO.crearPedido ERROR: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignore */ }
            return null;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            DBConnection.close(con);
        }
    }

    // ══════════════════════════════════════════════════════
    //  CAMBIAR ESTADO
    // ══════════════════════════════════════════════════════

    /**
     * Cambia el estado de un pedido con toda la lógica asociada:
     * - Si pasa a 'autorizado': marca primera_compra_usada = 1 si aplica
     * - Si pasa a 'cancelado': restaura el stock de todas las tallas
     */
    public boolean cambiarEstado(int pedidoId, String nuevoEstado, String notaAdmin) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Obtener estado actual y usuario del pedido
            Pedido pedido = buscarPorId(pedidoId);
            if (pedido == null) return false;
            if (Pedido.ESTADO_ENTREGADO.equals(pedido.getEstado())) return false;
            if (Pedido.ESTADO_CANCELADO.equals(pedido.getEstado())) return false;

            // Si es cancelación: restaurar stock
            if (Pedido.ESTADO_CANCELADO.equals(nuevoEstado)) {
                restaurarStockPedido(pedidoId, con);
            }

            // Actualizar estado del pedido
            String sql = "UPDATE pedidos SET estado = ?, "
                       + "nota_admin = COALESCE(?, nota_admin), "
                       + "fecha_actualizacion = NOW() WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            if (notaAdmin != null && !notaAdmin.isEmpty()) {
                ps.setString(2, notaAdmin);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setInt(3, pedidoId);
            ps.executeUpdate();

            // Si pasa a autorizado: marcar primera compra usada
            if (Pedido.ESTADO_AUTORIZADO.equals(nuevoEstado)
                    && pedido.getUsuarioId() != null
                    && pedido.getUsuarioId() > 0) {
                String sqlDescuento = "UPDATE usuarios SET primera_compra_usada = 1 "
                        + "WHERE id = ? AND primera_compra_usada = 0 "
                        + "AND DATEDIFF(NOW(), fecha_registro) <= 10";
                PreparedStatement psDesc = con.prepareStatement(sqlDescuento);
                psDesc.setInt(1, pedido.getUsuarioId());
                psDesc.executeUpdate();
            }

            con.commit();

            // Recalcular estado de productos si se canceló
            if (Pedido.ESTADO_CANCELADO.equals(nuevoEstado)) {
                List<DetallePedido> detalles = listarDetalles(pedidoId);
                for (DetallePedido d : detalles) {
                    productoDAO.recalcularEstado(d.getProductoId());
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("PedidoDAO.cambiarEstado ERROR: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
            DBConnection.close(con);
        }
    }

    // ══════════════════════════════════════════════════════
    //  CONSULTAS
    // ══════════════════════════════════════════════════════

    public Pedido buscarPorId(int id) {
        String sql = "SELECT * FROM vista_pedidos WHERE id = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pedido p = mapear(rs);
                p.setDetalles(listarDetalles(id));
                return p;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("PedidoDAO.buscarPorId: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    public Pedido buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM vista_pedidos WHERE codigo_pedido = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigo.toUpperCase().trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pedido p = mapear(rs);
                p.setDetalles(listarDetalles(p.getId()));
                return p;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("PedidoDAO.buscarPorCodigo: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    /** Rastreo sin login: código + email */
    public Pedido rastrear(String codigo, String email) {
        String sql = "SELECT * FROM vista_pedidos WHERE codigo_pedido = ? AND email_cliente = ?";
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigo.toUpperCase().trim());
            ps.setString(2, email.toLowerCase().trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pedido p = mapear(rs);
                p.setDetalles(listarDetalles(p.getId()));
                return p;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("PedidoDAO.rastrear: " + e.getMessage());
            return null;
        } finally {
            DBConnection.close(con);
        }
    }

    /** Historial de pedidos de un usuario registrado */
    public List<Pedido> listarPorUsuario(int usuarioId) {
        String sql = "SELECT * FROM vista_pedidos WHERE usuario_id = ? ORDER BY fecha DESC";
        List<Pedido> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("PedidoDAO.listarPorUsuario: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    /** Todos los pedidos para el panel admin */
    public List<Pedido> listarTodos() {
        return listarConFiltroEstado(null);
    }

    public List<Pedido> listarConFiltroEstado(String estado) {
        String sql = "SELECT * FROM vista_pedidos"
                + (estado != null ? " WHERE estado = ?" : "")
                + " ORDER BY fecha DESC";
        List<Pedido> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            if (estado != null) ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("PedidoDAO.listarConFiltroEstado: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    /** Detalles de un pedido con nombre de producto y talla */
    public List<DetallePedido> listarDetalles(int pedidoId) {
        String sql = "SELECT * FROM vista_detalle_pedido WHERE pedido_id = ?";
        List<DetallePedido> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido d = new DetallePedido();
                d.setId(rs.getInt("id"));
                d.setPedidoId(rs.getInt("pedido_id"));
                d.setProductoId(rs.getInt("producto_id"));
                d.setTallaId(rs.getInt("talla_id"));
                d.setNombreProducto(rs.getString("nombre_producto"));
                d.setTalla(rs.getString("talla"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                d.setDescuentoAplicado(rs.getInt("descuento_aplicado"));
                d.setImagenProducto(rs.getString("imagen_producto"));
                lista.add(d);
            }
        } catch (SQLException e) {
            System.err.println("PedidoDAO.listarDetalles: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return lista;
    }

    /** KPIs del dashboard admin */
    public java.util.Map<String, Object> obtenerKpisHoy() {
        String sql = "SELECT * FROM vista_kpi_hoy";
        java.util.Map<String, Object> kpis = new java.util.HashMap<>();
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            if (rs.next()) {
                kpis.put("totalPedidosHoy", rs.getInt("total_pedidos_hoy"));
                kpis.put("ventasHoy", rs.getBigDecimal("ventas_hoy"));
                kpis.put("pendientes", rs.getInt("pendientes"));
                kpis.put("autorizados", rs.getInt("autorizados"));
                kpis.put("preparados", rs.getInt("preparados"));
                kpis.put("enCamino", rs.getInt("en_camino"));
                kpis.put("entregados", rs.getInt("entregados"));
                kpis.put("cancelados", rs.getInt("cancelados"));
            }
        } catch (SQLException e) {
            System.err.println("PedidoDAO.obtenerKpisHoy: " + e.getMessage());
        } finally {
            DBConnection.close(con);
        }
        return kpis;
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════

    private String generarCodigoUnico(String prefijo, Connection con) throws SQLException {
        String sql = "SELECT codigo_pedido FROM pedidos WHERE codigo_pedido = ?";
        String codigo;
        do {
            int numero = (int)(Math.random() * 99999) + 1;
            codigo = prefijo + "-" + String.format("%05d", numero);
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, codigo);
            if (!ps.executeQuery().next()) break;
        } while (true);
        return codigo;
    }

    private void restaurarStockPedido(int pedidoId, Connection con) throws SQLException {
        String sql = "UPDATE tallas_producto tp "
                   + "JOIN detalle_pedido dp ON dp.talla_id = tp.id "
                   + "SET tp.stock = tp.stock + dp.cantidad "
                   + "WHERE dp.pedido_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, pedidoId);
        ps.executeUpdate();
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setCodigoPedido(rs.getString("codigo_pedido"));
        int uid = rs.getInt("usuario_id");
        p.setUsuarioId(rs.wasNull() ? null : uid);
        p.setNombreCliente(rs.getString("nombre_cliente"));
        p.setApellidoCliente(rs.getString("apellido_cliente"));
        p.setTelefonoCliente(rs.getString("telefono_cliente"));
        p.setEmailCliente(rs.getString("email_cliente"));
        p.setSubtotal(rs.getBigDecimal("subtotal"));
        p.setDescuentoPrimeraCompra(rs.getBigDecimal("descuento_primera_compra"));
        p.setCostoEnvio(rs.getBigDecimal("costo_envio"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setTipoPago(rs.getString("tipo_pago"));
        p.setTipoEntrega(rs.getString("tipo_entrega"));
        p.setDireccion(rs.getString("direccion"));
        p.setEstado(rs.getString("estado"));
        p.setNotaAdmin(rs.getString("nota_admin"));
        p.setFecha(rs.getTimestamp("fecha"));
        p.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
        return p;
    }
}
