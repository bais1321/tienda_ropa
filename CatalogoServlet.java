package servlet;

import dao.ProductoDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String categoria        = req.getParameter("categoria");
        String subcategoria     = req.getParameter("subcategoria");
        String busqueda         = req.getParameter("q");
        boolean soloDescuento   = "true".equals(req.getParameter("descuento"));

        req.setAttribute("productos",       productoDAO.listarConFiltros(categoria, subcategoria, busqueda, soloDescuento));
        req.setAttribute("categoriaActual", categoria);
        req.setAttribute("subActual",       subcategoria);
        req.setAttribute("busqueda",        busqueda);
        req.setAttribute("soloDescuento",   soloDescuento);
        req.getRequestDispatcher("/jsp/publico/catalogo.jsp").forward(req, resp);
    }
}
