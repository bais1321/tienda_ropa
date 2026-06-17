package servlet.admin;

import dao.PedidoDAO;
import dao.ProductoDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private PedidoDAO   pedidoDAO   = new PedidoDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("kpis",          pedidoDAO.obtenerKpisHoy());
        req.setAttribute("pedidosRecientes", pedidoDAO.listarConFiltroEstado("pendiente"));
        req.setAttribute("stockBajo",     productoDAO.listarStockBajo());
        req.getRequestDispatcher("/jsp/admin/dashboard.jsp").forward(req, resp);
    }
}
