package servlet;

import dao.ProductoDAO;
import modelo.ItemCarrito;
import modelo.Producto;
import modelo.TallaProducto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/carrito")
public class CarritoServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("items", obtenerCarrito(req));
        req.getRequestDispatcher("/jsp/publico/carrito.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");

        switch (accion != null ? accion : "") {
            case "agregar":   agregar(req, resp);   break;
            case "eliminar":  eliminar(req, resp);  break;
            case "actualizar":actualizar(req, resp);break;
            case "vaciar":    vaciar(req, resp);    break;
            default: resp.sendRedirect(req.getContextPath() + "/carrito");
        }
    }

    // ─── Agregar producto al carrito ──────────────────────
    private void agregar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            int productoId = Integer.parseInt(req.getParameter("productoId"));
            int tallaId    = Integer.parseInt(req.getParameter("tallaId"));
            int cantidad   = Integer.parseInt(req.getParameter("cantidad"));

            if (cantidad < 1) cantidad = 1;

            Producto producto = productoDAO.buscarPorId(productoId);
            if (producto == null || !producto.isDisponible()) {
                setToast(req, "error", "Producto no disponible.");
                resp.sendRedirect(req.getContextPath() + "/carrito");
                return;
            }

            // Buscar talla y validar stock
            TallaProducto talla = null;
            for (TallaProducto t : producto.getTallas()) {
                if (t.getId() == tallaId) { talla = t; break; }
            }
            if (talla == null || !talla.isDisponible()) {
                setToast(req, "error", "Talla no disponible.");
                resp.sendRedirect(req.getContextPath() + "/producto?id=" + productoId);
                return;
            }

            // Validar cantidad vs stock
            List<ItemCarrito> carrito = obtenerCarrito(req);
            int yaEnCarrito = 0;
            String clave = productoId + "_" + tallaId;
            for (ItemCarrito it : carrito) {
                if (it.getClave().equals(clave)) { yaEnCarrito = it.getCantidad(); break; }
            }
            if (yaEnCarrito + cantidad > talla.getStock()) {
                setToast(req, "error", "No hay suficiente stock. Máximo disponible: " + talla.getStock());
                resp.sendRedirect(req.getContextPath() + "/producto?id=" + productoId);
                return;
            }

            // Agregar o sumar al carrito
            boolean encontrado = false;
            for (ItemCarrito item : carrito) {
                if (item.getClave().equals(clave)) {
                    item.setCantidad(item.getCantidad() + cantidad);
                    encontrado = true; break;
                }
            }
            if (!encontrado) {
                carrito.add(new ItemCarrito(
                    productoId, producto.getNombre(),
                    tallaId, talla.getTalla(),
                    cantidad, producto.getPrecioFinal(),
                    producto.getDescuento(),
                    producto.getRutaImagenPrincipal(),
                    talla.getStock()
                ));
            }
            guardarCarrito(req, carrito);
            setToast(req, "exito", "Producto agregado al carrito.");
        } catch (NumberFormatException e) {
            setToast(req, "error", "Datos inválidos.");
        }
        resp.sendRedirect(req.getContextPath() + "/carrito");
    }

    // ─── Eliminar ítem del carrito ────────────────────────
    private void eliminar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String clave = req.getParameter("clave");
        List<ItemCarrito> carrito = obtenerCarrito(req);
        carrito.removeIf(it -> it.getClave().equals(clave));
        guardarCarrito(req, carrito);
        setToast(req, "exito", "Producto eliminado del carrito.");
        resp.sendRedirect(req.getContextPath() + "/carrito");
    }

    // ─── Actualizar cantidad ──────────────────────────────
    private void actualizar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            String clave    = req.getParameter("clave");
            int cantidad    = Integer.parseInt(req.getParameter("cantidad"));
            List<ItemCarrito> carrito = obtenerCarrito(req);
            for (ItemCarrito item : carrito) {
                if (item.getClave().equals(clave)) {
                    int maxStock = productoDAO.buscarTallaPorId(item.getTallaId()) != null
                            ? productoDAO.buscarTallaPorId(item.getTallaId()).getStock() : 0;
                    // Validar límite de stock
                    if (cantidad > maxStock) cantidad = maxStock;
                    if (cantidad < 1) {
                        carrito.remove(item); break;
                    }
                    item.setCantidad(cantidad);
                    break;
                }
            }
            guardarCarrito(req, carrito);
        } catch (NumberFormatException e) { /* ignorar */ }
        resp.sendRedirect(req.getContextPath() + "/carrito");
    }

    // ─── Vaciar carrito ───────────────────────────────────
    private void vaciar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        guardarCarrito(req, new ArrayList<>());
        resp.sendRedirect(req.getContextPath() + "/carrito");
    }

    // ─── Helpers de sesión ────────────────────────────────
    @SuppressWarnings("unchecked")
    public static List<ItemCarrito> obtenerCarrito(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    public static void guardarCarrito(HttpServletRequest req, List<ItemCarrito> carrito) {
        req.getSession(true).setAttribute("carrito", carrito);
    }

    public static void vaciarCarritoSesion(HttpServletRequest req) {
        req.getSession(true).removeAttribute("carrito");
    }

    public static int contarItems(HttpServletRequest req) {
        return obtenerCarrito(req).stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    private void setToast(HttpServletRequest req, String tipo, String mensaje) {
        req.getSession(true).setAttribute("toastTipo", tipo);
        req.getSession(true).setAttribute("toast", mensaje);
    }

    // Expuesto para que PedidoServlet pueda obtener la talla por id
    public TallaProducto buscarTallaPorId(int id) {
        return productoDAO.buscarTallaPorId(id);
    }
}
