package servlet.admin;

import dao.ProductoDAO;
import modelo.ImagenProducto;
import modelo.Producto;
import modelo.TallaProducto;
import util.SubidaImagenUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/admin/productos")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB
    maxFileSize       = 5 * 1024 * 1024,   // 5 MB por imagen
    maxRequestSize    = 25 * 1024 * 1024   // 25 MB total (5 imágenes)
)
public class ProductoAdminServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");

        if ("editar".equals(accion)) {
            // Mostrar formulario de edición
            int id = Integer.parseInt(req.getParameter("id"));
            Producto p = productoDAO.buscarPorId(id);
            if (p == null) { resp.sendRedirect(req.getContextPath() + "/admin/productos"); return; }
            req.setAttribute("producto", p);
            req.getRequestDispatcher("/jsp/admin/formulario_producto.jsp").forward(req, resp);

        } else if ("nuevo".equals(accion)) {
            // Formulario vacío para nuevo producto
            req.getRequestDispatcher("/jsp/admin/formulario_producto.jsp").forward(req, resp);

        } else if ("eliminarImagen".equals(accion)) {
            // Eliminar imagen específica
            int imgId = Integer.parseInt(req.getParameter("imgId"));
            int productoId = Integer.parseInt(req.getParameter("productoId"));
            ImagenProducto img = productoDAO.listarImagenesPorProducto(productoId)
                    .stream().filter(i -> i.getId() == imgId).findFirst().orElse(null);
            if (img != null) {
                SubidaImagenUtil.eliminarImagen(img.getRutaImagen(),
                        req.getServletContext().getRealPath("/"));
                productoDAO.eliminarImagen(imgId);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/productos?accion=editar&id=" + productoId);

        } else if ("eliminarTalla".equals(accion)) {
            int tallaId    = Integer.parseInt(req.getParameter("tallaId"));
            int productoId = Integer.parseInt(req.getParameter("productoId"));
            productoDAO.eliminarTalla(tallaId);
            resp.sendRedirect(req.getContextPath() + "/admin/productos?accion=editar&id=" + productoId);

        } else {
            // Listado de todos los productos
            req.setAttribute("productos", productoDAO.listarTodos());
            req.getRequestDispatcher("/jsp/admin/productos.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");

        switch (accion != null ? accion : "") {
            case "crear":        crearProducto(req, resp);   break;
            case "actualizar":   actualizarProducto(req, resp); break;
            case "cambiarEstado":cambiarEstado(req, resp);   break;
            case "agregarTalla": agregarTalla(req, resp);    break;
            case "actualizarStockTalla": actualizarStock(req, resp); break;
            default: resp.sendRedirect(req.getContextPath() + "/admin/productos");
        }
    }

    // ─── Crear producto nuevo ─────────────────────────────
    private void crearProducto(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        try {
            Producto p = leerFormulario(req);
            int nuevoId = productoDAO.crear(p);
            if (nuevoId <= 0) throw new Exception("Error al crear producto en BD.");

            // Subir imágenes
            subirImagenes(req, nuevoId);

            // Agregar tallas si vienen en el formulario
            agregarTallasDesdeFormulario(req, nuevoId);

            req.getSession().setAttribute("toast", "Producto creado exitosamente.");
            resp.sendRedirect(req.getContextPath() + "/admin/productos?accion=editar&id=" + nuevoId);

        } catch (Exception e) {
            req.setAttribute("error", "Error al crear el producto: " + e.getMessage());
            req.getRequestDispatcher("/jsp/admin/formulario_producto.jsp").forward(req, resp);
        }
    }

    // ─── Actualizar producto existente ────────────────────
    private void actualizarProducto(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Producto p = leerFormulario(req);
            p.setId(id);
            productoDAO.actualizar(p);

            // Subir nuevas imágenes (si se subieron)
            subirImagenes(req, id);

            req.getSession().setAttribute("toast", "Producto actualizado correctamente.");
            resp.sendRedirect(req.getContextPath() + "/admin/productos?accion=editar&id=" + id);

        } catch (Exception e) {
            req.setAttribute("error", "Error al actualizar: " + e.getMessage());
            req.getRequestDispatcher("/jsp/admin/formulario_producto.jsp").forward(req, resp);
        }
    }

    // ─── Cambiar estado (activo/agotado/inactivo) ─────────
    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id     = Integer.parseInt(req.getParameter("id"));
        String est = req.getParameter("estado");
        productoDAO.cambiarEstado(id, est);
        req.getSession().setAttribute("toast", "Estado actualizado.");
        resp.sendRedirect(req.getContextPath() + "/admin/productos");
    }

    // ─── Agregar talla a un producto existente ────────────
    private void agregarTalla(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int    productoId = Integer.parseInt(req.getParameter("productoId"));
        String talla      = req.getParameter("talla");
        int    stock      = Integer.parseInt(req.getParameter("stock"));

        TallaProducto t = new TallaProducto(productoId, talla.toUpperCase().trim(), stock);
        productoDAO.agregarTalla(t);
        productoDAO.recalcularEstado(productoId);

        req.getSession().setAttribute("toast", "Talla agregada.");
        resp.sendRedirect(req.getContextPath() + "/admin/productos?accion=editar&id=" + productoId);
    }

    // ─── Actualizar stock de una talla ────────────────────
    private void actualizarStock(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int tallaId    = Integer.parseInt(req.getParameter("tallaId"));
        int productoId = Integer.parseInt(req.getParameter("productoId"));
        int nuevoStock = Integer.parseInt(req.getParameter("stock"));

        productoDAO.actualizarStockTalla(tallaId, nuevoStock);
        productoDAO.recalcularEstado(productoId);

        req.getSession().setAttribute("toast", "Stock actualizado.");
        resp.sendRedirect(req.getContextPath() + "/admin/productos?accion=editar&id=" + productoId);
    }

    // ─── Helpers ──────────────────────────────────────────

    private Producto leerFormulario(HttpServletRequest req) {
        Producto p = new Producto();
        p.setNombre(req.getParameter("nombre").trim());
        p.setDescripcion(req.getParameter("descripcion"));
        p.setPrecioOriginal(new BigDecimal(req.getParameter("precioOriginal")));
        p.setDescuento(Integer.parseInt(req.getParameter("descuento") != null
                ? req.getParameter("descuento") : "0"));
        p.setCategoria(req.getParameter("categoria"));
        p.setSubcategoria(req.getParameter("subcategoria"));
        p.setEsDestacado("on".equals(req.getParameter("esDestacado")));
        p.setEstado(req.getParameter("estado") != null ? req.getParameter("estado") : "activo");
        return p;
    }

    private void subirImagenes(HttpServletRequest req, int productoId)
            throws IOException, ServletException {
        String rutaApp = req.getServletContext().getRealPath("/");
        int orden = productoDAO.contarImagenes(productoId);

        for (Part part : req.getParts()) {
            if (!"imagenes".equals(part.getName())) continue;
            if (part.getSize() == 0) continue;
            try {
                String ruta = SubidaImagenUtil.subirImagen(part, rutaApp);
                if (ruta != null) {
                    productoDAO.agregarImagen(new ImagenProducto(productoId, ruta, orden++));
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Imagen inválida ignorada: " + e.getMessage());
            }
        }
    }

    private void agregarTallasDesdeFormulario(HttpServletRequest req, int productoId) {
        String[] tallasArr = req.getParameterValues("tallaNombre");
        String[] stocksArr = req.getParameterValues("tallaStock");
        if (tallasArr == null || stocksArr == null) return;
        for (int i = 0; i < tallasArr.length; i++) {
            if (tallasArr[i] == null || tallasArr[i].trim().isEmpty()) continue;
            try {
                int stock = Integer.parseInt(stocksArr[i]);
                productoDAO.agregarTalla(
                    new TallaProducto(productoId, tallasArr[i].toUpperCase().trim(), stock));
            } catch (NumberFormatException e) { /* ignorar */ }
        }
    }

    // Expuesto para que el JSP pueda llamar buscarTalla
    public TallaProducto buscarTallaPorId(int id) {
        return productoDAO.buscarTallaPorId(id);
    }
}
