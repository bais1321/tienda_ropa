package servlet.admin;

import dao.ConfigDAO;
import dao.NotificacionDAO;
import dao.PedidoDAO;
import modelo.*;
import util.MailUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/admin/pedidos")
public class PedidoAdminServlet extends HttpServlet {

    private PedidoDAO       pedidoDAO       = new PedidoDAO();
    private ConfigDAO       configDAO       = new ConfigDAO();
    private NotificacionDAO notificacionDAO = new NotificacionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");

        if ("ver".equals(accion)) {
            // Detalle de un pedido
            int id = Integer.parseInt(req.getParameter("id"));
            Pedido pedido = pedidoDAO.buscarPorId(id);
            if (pedido == null) { resp.sendRedirect(req.getContextPath() + "/admin/pedidos"); return; }
            req.setAttribute("pedido",          pedido);
            req.setAttribute("notificaciones",  notificacionDAO.listarPorPedido(id));
            req.setAttribute("config",          configDAO.obtener());
            req.getRequestDispatcher("/jsp/admin/detalle_pedido.jsp").forward(req, resp);

        } else {
            // Listado con filtro por estado
            String estado = req.getParameter("estado");
            req.setAttribute("pedidos",        pedidoDAO.listarConFiltroEstado(
                    (estado != null && !estado.isEmpty()) ? estado : null));
            req.setAttribute("estadoFiltro",   estado);
            req.getRequestDispatcher("/jsp/admin/pedidos.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");

        switch (accion != null ? accion : "") {
            case "cambiarEstado": cambiarEstado(req, resp);  break;
            case "enviarWA":      generarLinkWA(req, resp);  break;
            default: resp.sendRedirect(req.getContextPath() + "/admin/pedidos");
        }
    }

    // ─── Cambiar estado del pedido ─────────────────────────
    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int    pedidoId    = Integer.parseInt(req.getParameter("pedidoId"));
        String nuevoEstado = req.getParameter("nuevoEstado");
        String notaAdmin   = req.getParameter("notaAdmin");

        boolean ok = pedidoDAO.cambiarEstado(pedidoId, nuevoEstado, notaAdmin);

        if (ok) {
            // Recargar pedido actualizado y enviar notificaciones
            Pedido pedido = pedidoDAO.buscarPorId(pedidoId);
            ConfigTienda config = configDAO.obtener();
            enviarNotificaciones(pedido, config);
            req.getSession().setAttribute("toast", "Estado actualizado a: " + pedido.getEstadoLabel());
        } else {
            req.getSession().setAttribute("toastTipo", "error");
            req.getSession().setAttribute("toast", "No se pudo cambiar el estado.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/pedidos?accion=ver&id=" + pedidoId);
    }

    // ─── Generar link de WhatsApp para notificar al cliente ──
    private void generarLinkWA(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int pedidoId = Integer.parseInt(req.getParameter("pedidoId"));
        Pedido pedido = pedidoDAO.buscarPorId(pedidoId);
        ConfigTienda config = configDAO.obtener();

        String mensaje = construirMensajeWA(pedido, config);
        String link = config.getLinkWhatsappNotificacion(
                pedido.getTelefonoCliente(), mensaje);

        // Guardar en historial
        Notificacion n = new Notificacion(pedidoId, Notificacion.TIPO_WHATSAPP,
                pedido.getTelefonoCliente(), null, mensaje, Notificacion.ESTADO_ENVIADO);
        notificacionDAO.guardar(n);

        // Redirigir al link de WhatsApp (se abre en nueva pestaña desde el JSP)
        req.getSession().setAttribute("linkWA", link);
        resp.sendRedirect(req.getContextPath() + "/admin/pedidos?accion=ver&id=" + pedidoId);
    }

    // ─── Enviar notificaciones automáticas ────────────────
    private void enviarNotificaciones(Pedido pedido, ConfigTienda config) {
        // Email al cliente
        try {
            String asunto = "Actualización de pedido — " + pedido.getCodigoPedido()
                          + " [" + pedido.getEstadoLabel() + "]";
            String cuerpo = MailUtil.plantillaCambioEstado(pedido, config);
            boolean enviado = MailUtil.enviar(config, pedido.getEmailCliente(), asunto, cuerpo);
            notificacionDAO.guardar(new Notificacion(
                    pedido.getId(), Notificacion.TIPO_EMAIL,
                    pedido.getEmailCliente(), asunto, cuerpo,
                    enviado ? Notificacion.ESTADO_ENVIADO : Notificacion.ESTADO_FALLIDO));
        } catch (Exception e) {
            System.err.println("PedidoAdminServlet: error email: " + e.getMessage());
        }
    }

    private String construirMensajeWA(Pedido p, ConfigTienda config) {
        switch (p.getEstado()) {
            case Pedido.ESTADO_AUTORIZADO:
                return "Hola " + p.getNombreCliente() + ", tu pago fue confirmado. "
                     + "Estamos preparando tu pedido " + p.getCodigoPedido() + ". ¡Gracias!";
            case Pedido.ESTADO_PREPARADO:
                return "Hola " + p.getNombreCliente() + ", tu pedido "
                     + p.getCodigoPedido() + " está listo.";
            case Pedido.ESTADO_EN_CAMINO:
                String nota = (p.getNotaAdmin() != null && !p.getNotaAdmin().isEmpty())
                        ? " Detalles: " + p.getNotaAdmin() : "";
                return Pedido.ENTREGA_DOMICILIO.equals(p.getTipoEntrega())
                    ? "Hola " + p.getNombreCliente() + ", tu pedido " + p.getCodigoPedido()
                      + " está en camino." + nota
                    : "Hola " + p.getNombreCliente() + ", tu pedido " + p.getCodigoPedido()
                      + " está listo para recoger." + nota;
            case Pedido.ESTADO_ENTREGADO:
                return "Hola " + p.getNombreCliente() + ", confirmamos que tu pedido "
                     + p.getCodigoPedido() + " fue entregado. ¡Gracias por tu compra!";
            case Pedido.ESTADO_CANCELADO:
                return "Hola " + p.getNombreCliente() + ", lamentamos informarte que tu pedido "
                     + p.getCodigoPedido() + " fue cancelado. Contáctanos para más información.";
            default:
                return "Hola " + p.getNombreCliente() + ", tu pedido " + p.getCodigoPedido()
                     + " ha sido actualizado. Estado: " + p.getEstadoLabel();
        }
    }
}
