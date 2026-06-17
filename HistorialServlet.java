package servlet;

import dao.PedidoDAO;
import modelo.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/historial")
public class HistorialServlet extends HttpServlet {

    private PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
        req.setAttribute("pedidos", pedidoDAO.listarPorUsuario(usuario.getId()));
        req.getRequestDispatcher("/jsp/usuario/historial.jsp").forward(req, resp);
    }
}
