package servlet;

import dao.ConfigDAO;
import modelo.ConfigTienda;
import modelo.ItemCarrito;
import modelo.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private ConfigDAO configDAO = new ConfigDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<ItemCarrito> carrito = CarritoServlet.obtenerCarrito(req);
        if (carrito.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/carrito");
            return;
        }

        ConfigTienda config = configDAO.obtener();
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");

        // Calcular subtotal
        BigDecimal subtotal = carrito.stream()
                .map(ItemCarrito::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular descuento 5% primera compra
        BigDecimal descuento5 = BigDecimal.ZERO;
        boolean puedeDescuento5 = (usuario != null
                && usuario.puedeUsarDescuentoPrimeraCompra());
        if (puedeDescuento5) {
            descuento5 = subtotal
                    .multiply(new BigDecimal("0.05"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        /*
         * SOLUCIÓN DEFINITIVA:
         * En lugar de pasar el objeto ConfigTienda al JSP (lo que causaba el error
         * de cast), se extraen SOLO los valores que necesita el JSP como tipos
         * simples: BigDecimal y String. El JSP nunca recibe ni castea ConfigTienda.
         */
        BigDecimal costoEnvio      = config.getCostoEnvioDomicilio();
        BigDecimal minimoDomicilio = config.getMinimoDomicilio();
        String     datosBancarios  = config.getDatosBancarios() != null
                                     ? config.getDatosBancarios()
                                     : "Configura los datos bancarios en el panel admin.";

        req.setAttribute("carrito",         carrito);
        req.setAttribute("subtotal",        subtotal);
        req.setAttribute("descuento5",      descuento5);
        req.setAttribute("puedeDesc5",      puedeDescuento5);
        req.setAttribute("costoEnvio",      costoEnvio);      // BigDecimal
        req.setAttribute("minimoDomicilio", minimoDomicilio); // BigDecimal
        req.setAttribute("datosBancarios",  datosBancarios);  // String
        req.setAttribute("usuario",         usuario);
        // ConfigTienda NO se pasa al JSP

        req.getRequestDispatcher("/jsp/publico/checkout.jsp")
           .forward(req, resp);
    }
}
