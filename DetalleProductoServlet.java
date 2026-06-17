package servlet;

import dao.ProductoDAO;
import modelo.Producto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/producto")
public class DetalleProductoServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/catalogo");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Producto producto = productoDAO.buscarPorId(id);
            if (producto == null || "inactivo".equals(producto.getEstado())) {
                resp.sendRedirect(req.getContextPath() + "/catalogo");
                return;
            }
            req.setAttribute("producto", producto);
            req.setAttribute("relacionados", productoDAO.listarRelacionados(
                    id, producto.getCategoria(), producto.getSubcategoria(), 4));
            req.getRequestDispatcher("/jsp/publico/detalle.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/catalogo");
        }
    }
}
