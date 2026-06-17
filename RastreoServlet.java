package servlet;

import dao.PedidoDAO;
import modelo.Pedido;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/rastreo")
public class RastreoServlet extends HttpServlet {

    private PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Puede venir con parámetros desde el link de confirmación
        String codigo = req.getParameter("codigo");
        String email  = req.getParameter("email");

        if (codigo != null && email != null && !codigo.isEmpty() && !email.isEmpty()) {
            buscarYMostrar(req, resp, codigo, email);
        } else {
            req.getRequestDispatcher("/jsp/publico/rastreo.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String codigo = req.getParameter("codigo");
        String email  = req.getParameter("email");

        if (codigo == null || codigo.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {
            req.setAttribute("error", "Ingresa el código de pedido y tu correo electrónico.");
            req.getRequestDispatcher("/jsp/publico/rastreo.jsp").forward(req, resp);
            return;
        }
        buscarYMostrar(req, resp, codigo.trim(), email.trim().toLowerCase());
    }

    private void buscarYMostrar(HttpServletRequest req, HttpServletResponse resp,
            String codigo, String email) throws ServletException, IOException {
        Pedido pedido = pedidoDAO.rastrear(codigo, email);
        if (pedido == null) {
            req.setAttribute("error",
                    "No encontramos un pedido con ese código y correo. Verifica los datos.");
            req.setAttribute("codigoIngresado", codigo);
            req.setAttribute("emailIngresado",  email);
        } else {
            req.setAttribute("pedido", pedido);
        }
        req.getRequestDispatcher("/jsp/publico/rastreo.jsp").forward(req, resp);
    }
}
