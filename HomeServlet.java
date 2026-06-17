package servlet;

import dao.ConfigDAO;
import dao.ProductoDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("")
public class HomeServlet extends HttpServlet {

    private ProductoDAO productoDAO = new ProductoDAO();
    private ConfigDAO   configDAO   = new ConfigDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("destacados", productoDAO.listarDestacados());
        req.setAttribute("config",     configDAO.obtener());
        req.getRequestDispatcher("/jsp/publico/home.jsp").forward(req, resp);
    }
}
