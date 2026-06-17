package servlet;

import dao.ConfigDAO;
import dao.NotificacionDAO;
import dao.PedidoDAO;
import modelo.*;
import util.MailUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@WebServlet("/pedido")
public class PedidoServlet extends HttpServlet {

    private PedidoDAO       pedidoDAO       = new PedidoDAO();
    private ConfigDAO       configDAO       = new ConfigDAO();
    private NotificacionDAO notificacionDAO = new NotificacionDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        List<ItemCarrito> carrito = CarritoServlet.obtenerCarrito(req);
        if (carrito.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/carrito");
            return;
        }

        ConfigTienda config = configDAO.obtener();
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");

        // ─── Leer datos del formulario ─────────────────────
        String nombre      = req.getParameter("nombre");
        String apellido    = req.getParameter("apellido");
        String telefono    = req.getParameter("telefono");
        String email       = req.getParameter("email");
        String tipoPago    = req.getParameter("tipoPago");
        String tipoEntrega = req.getParameter("tipoEntrega");
        String direccion   = req.getParameter("direccion");

        // ─── Validaciones básicas ──────────────────────────
        if (nombre == null || nombre.trim().isEmpty()
                || apellido == null || apellido.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || tipoPago == null || tipoEntrega == null) {
            req.getSession().setAttribute("toastTipo", "error");
            req.getSession().setAttribute("toast", "Por favor completa todos los datos.");
            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        // Validar dirección si es domicilio
        if (Pedido.ENTREGA_DOMICILIO.equals(tipoEntrega)
                && (direccion == null || direccion.trim().isEmpty())) {
            req.getSession().setAttribute("toastTipo", "error");
            req.getSession().setAttribute("toast", "Ingresa tu dirección de entrega.");
            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        // ─── Calcular montos en el servidor (nunca confiar en el front) ──
        BigDecimal subtotal = carrito.stream()
                .map(ItemCarrito::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Descuento 5% primera compra
        BigDecimal descuento5 = BigDecimal.ZERO;
        boolean puedeDesc5 = (usuario != null && usuario.puedeUsarDescuentoPrimeraCompra());
        if (puedeDesc5) {
            descuento5 = subtotal.multiply(new BigDecimal("0.05"))
                                  .setScale(2, RoundingMode.HALF_UP);
        }

        // Costo de envío
        BigDecimal costoEnvio = BigDecimal.ZERO;
        if (Pedido.ENTREGA_DOMICILIO.equals(tipoEntrega)) {
            BigDecimal totalParaMinimo = subtotal.subtract(descuento5);
            if (totalParaMinimo.compareTo(config.getMinimoDomicilio()) < 0) {
                req.getSession().setAttribute("toastTipo", "error");
                req.getSession().setAttribute("toast",
                        "El total mínimo para envío a domicilio es Q"
                        + config.getMinimoDomicilio() + ".");
                resp.sendRedirect(req.getContextPath() + "/checkout");
                return;
            }
            costoEnvio = config.getCostoEnvioDomicilio();
        }

        BigDecimal total = subtotal.subtract(descuento5).add(costoEnvio)
                                   .setScale(2, RoundingMode.HALF_UP);

        // ─── Construir objeto Pedido ───────────────────────
        Pedido pedido = new Pedido();
        pedido.setNombreCliente(nombre.trim());
        pedido.setApellidoCliente(apellido.trim());
        pedido.setTelefonoCliente(telefono != null ? telefono.trim() : null);
        pedido.setEmailCliente(email.trim().toLowerCase());
        pedido.setTipoPago(tipoPago);
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setDireccion(Pedido.ENTREGA_DOMICILIO.equals(tipoEntrega) ? direccion.trim() : null);
        pedido.setSubtotal(subtotal);
        pedido.setDescuentoPrimeraCompra(descuento5);
        pedido.setCostoEnvio(costoEnvio);
        pedido.setTotal(total);

        if (usuario != null && !usuario.isEsAdmin()) {
            pedido.setUsuarioId(usuario.getId());
        }

        // ─── Generar pedido (transacción) ─────────────────
        Pedido creado = pedidoDAO.crearPedido(pedido, carrito);

        if (creado == null) {
            req.getSession().setAttribute("toastTipo", "error");
            req.getSession().setAttribute("toast",
                    "Error al procesar el pedido. Por favor verifica el stock e intenta nuevamente.");
            resp.sendRedirect(req.getContextPath() + "/checkout");
            return;
        }

        // ─── Vaciar carrito ────────────────────────────────
        CarritoServlet.vaciarCarritoSesion(req);

        // ─── Enviar notificación por email ─────────────────
        try {
            String asunto   = "Pedido recibido — " + creado.getCodigoPedido();
            String cuerpo   = MailUtil.plantillaPedidoCreado(creado, config);
            boolean enviado = MailUtil.enviar(config, creado.getEmailCliente(), asunto, cuerpo);
            Notificacion n  = new Notificacion(
                    creado.getId(), Notificacion.TIPO_EMAIL,
                    creado.getEmailCliente(), asunto, cuerpo,
                    enviado ? Notificacion.ESTADO_ENVIADO : Notificacion.ESTADO_FALLIDO);
            notificacionDAO.guardar(n);
        } catch (Exception e) {
            System.err.println("PedidoServlet: error enviando email: " + e.getMessage());
        }

        // ─── Guardar pedido en sesión para la página de confirmación ──
        req.getSession().setAttribute("pedidoConfirmado", creado);
        req.getSession().setAttribute("carritoGuardado", carrito); // Para mostrar el resumen

        resp.sendRedirect(req.getContextPath() + "/confirmacion");
    }
}
