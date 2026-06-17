package servlet;

import dao.ConfigDAO;
import modelo.ConfigTienda;
import modelo.ItemCarrito;
import modelo.Pedido;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/confirmacion")
public class ConfirmacionServlet extends HttpServlet {

    private ConfigDAO configDAO = new ConfigDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        Pedido pedido = (Pedido) session.getAttribute("pedidoConfirmado");
        if (pedido == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        @SuppressWarnings("unchecked")
        List<ItemCarrito> itemsResumen =
                (List<ItemCarrito>) session.getAttribute("carritoGuardado");

        ConfigTienda config = configDAO.obtener();

        /*
         * SOLUCIÓN DEFINITIVA:
         * En lugar de pasar el objeto ConfigTienda al JSP (lo que causaba el error
         * de cast), se extrae solo el String del link de WhatsApp aquí en el servlet
         * y se pasa como atributo simple. El JSP nunca necesita ver ConfigTienda.
         */
        String linkWhatsapp = null;
        if (pedido.esPorTransferencia() && config.getNumeroWhatsapp() != null) {
            linkWhatsapp = config.getLinkWhatsappComprobante(
                    pedido.getCodigoPedido(),
                    pedido.getNombreCompletoCliente());
        }

        // Limpiar sesión
        session.removeAttribute("pedidoConfirmado");
        session.removeAttribute("carritoGuardado");

        // Pasar solo tipos simples al JSP — sin ConfigTienda
        req.setAttribute("pedido",        pedido);
        req.setAttribute("itemsResumen",  itemsResumen);
        req.setAttribute("linkWhatsapp",  linkWhatsapp); // String simple

        req.getRequestDispatcher("/jsp/publico/confirmacion.jsp")
           .forward(req, resp);
    }
}
