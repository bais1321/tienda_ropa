<%@ page contentType="text/html;charset=UTF-8" %>
<%-- CORRECCIÓN línea 6: imports separados explícitamente para garantizar
     que ConfigTienda y todos los modelos estén disponibles --%>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="modelo.Pedido" %>
<%@ page import="modelo.DetallePedido" %>
<%@ page import="modelo.Notificacion" %>
<%@ page import="modelo.ConfigTienda" %>
<%
    Pedido pedido              = (Pedido)            request.getAttribute("pedido");
    List<Notificacion> notifs  = (List<Notificacion>) request.getAttribute("notificaciones");
    ConfigTienda config        = (Co nfigTienda)       request.getAttribute("config");
    String linkWA              = (String)             session.getAttribute("linkWA");
    if (linkWA != null) session.removeAttribute("linkWA");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Pedido <%= pedido.getCodigoPedido() %> — Admin</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
  <link rel="stylesheet" href="<%= ctx %>/css/admin.css">
</head>
<body class="admin-body">

<%@ include file="/jsp/admin/navbar_admin.jsp" %>

<% if (linkWA != null) { %>
<script>window.open('<%= linkWA %>', '_blank');</script>
<% } %>

<div class="admin-contenedor">
  <div class="admin-page-header">
    <div>
      <h1 class="admin-titulo">Pedido: <span class="codigo-inline"><%= pedido.getCodigoPedido() %></span></h1>
      <p class="texto-gris">Realizado el <%= pedido.getFecha() %> · Última actualización: <%= pedido.getFechaActualizacion() %></p>
    </div>
    <a href="<%= ctx %>/admin/pedidos" class="btn-secundario">← Volver</a>
  </div>

  <div class="admin-dos-col">

    <!-- ═══ COLUMNA IZQUIERDA ═══════════════════════════ -->
    <div>

      <!-- Info del cliente -->
      <div class="admin-card">
        <h3>👤 Datos del Cliente</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">Nombre</span>
            <span><%= pedido.getNombreCompletoCliente() %></span>
          </div>
          <div class="info-item">
            <span class="info-label">Correo</span>
            <span><%= pedido.getEmailCliente() %></span>
          </div>
          <% if (pedido.getTelefonoCliente() != null) { %>
          <div class="info-item">
            <span class="info-label">Teléfono</span>
            <span><%= pedido.getTelefonoCliente() %></span>
          </div>
          <% } %>
          <div class="info-item">
            <span class="info-label">Tipo</span>
            <span><%= pedido.esDeInvitado() ? "Invitado" : "Cliente registrado" %></span>
          </div>
        </div>
      </div>

      <!-- Productos del pedido -->
      <div class="admin-card">
        <h3>🛒 Productos del Pedido</h3>
        <%
          List<DetallePedido> detalles = pedido.getDetalles();
          if (detalles != null && !detalles.isEmpty()) {
            for (DetallePedido d : detalles) {
              String imgDetalle = d.getImagenProducto() != null
                      ? ctx + d.getImagenProducto()
                      : ctx + "/img/placeholder.jpg";
        %>
        <div class="resumen-item">
          <img src="<%= imgDetalle %>" alt="">
          <div class="resumen-item-info">
            <p><strong><%= d.getNombreProducto() %></strong></p>
            <p>Talla: <strong><%= d.getTalla() %></strong> · Cantidad: <%= d.getCantidad() %></p>
            <% if (d.getDescuentoAplicado() > 0) { %>
              <p class="texto-descuento">Descuento aplicado: <%= d.getDescuentoAplicado() %>%</p>
            <% } %>
          </div>
          <div class="resumen-item-precio">
            <p>Q<%= d.getPrecioUnitario() %> c/u</p>
            <p><strong>Q<%= d.getSubtotalLinea() %></strong></p>
          </div>
        </div>
        <%  }
          } %>

        <div class="resumen-totales">
          <div class="resumen-linea"><span>Subtotal</span><span>Q<%= pedido.getSubtotal() %></span></div>
          <% if (pedido.getDescuentoPrimeraCompra() != null
                 && pedido.getDescuentoPrimeraCompra().compareTo(BigDecimal.ZERO) > 0) { %>
          <div class="resumen-linea resumen-descuento">
            <span>Descuento primera compra</span>
            <span>-Q<%= pedido.getDescuentoPrimeraCompra() %></span>
          </div>
          <% } %>
          <div class="resumen-linea">
            <span>Costo de envío</span>
            <span><%= pedido.getCostoEnvio().compareTo(BigDecimal.ZERO) > 0
                      ? "Q" + pedido.getCostoEnvio() : "Gratis" %></span>
          </div>
          <div class="resumen-total">
            <span>TOTAL</span>
            <span class="precio-final">Q<%= pedido.getTotal() %></span>
          </div>
        </div>
      </div>

      <!-- Historial de notificaciones -->
      <div class="admin-card">
        <h3>🔔 Notificaciones Enviadas</h3>
        <% if (notifs == null || notifs.isEmpty()) { %>
          <p class="sin-datos">No se han enviado notificaciones.</p>
        <% } else { %>
        <table class="admin-tabla">
          <thead>
            <tr><th>Tipo</th><th>Destinatario</th><th>Estado</th><th>Fecha</th></tr>
          </thead>
          <tbody>
            <% for (Notificacion n : notifs) { %>
            <tr>
              <td><%= n.isEmail() ? "📧 Email" : "📱 WhatsApp" %></td>
              <td><small><%= n.getDestinatario() %></small></td>
              <td>
                <span class="badge-estado <%= n.fueEnviada() ? "badge-verde" : "badge-rojo" %>">
                  <%= n.getEstado() %>
                </span>
              </td>
              <td><small><%= n.getFecha() %></small></td>
            </tr>
            <% } %>
          </tbody>
        </table>
        <% } %>
      </div>
    </div>

    <!-- ═══ COLUMNA DERECHA ══════════════════════════════ -->
    <div>

      <!-- Estado actual + cambio -->
      <div class="admin-card">
        <h3>📋 Estado del Pedido</h3>
        <div class="estado-actual">
          <span class="badge-estado badge-<%= pedido.getEstado() %> badge-grande">
            <%= pedido.getEstadoLabel() %>
          </span>
        </div>

        <% if (!pedido.esCancelado() && !pedido.esEntregado()) { %>
        <form action="<%= ctx %>/admin/pedidos" method="post" class="form-cambio-estado">
          <input type="hidden" name="accion"   value="cambiarEstado">
          <input type="hidden" name="pedidoId" value="<%= pedido.getId() %>">

          <div class="form-grupo">
            <label>Cambiar a:</label>
            <select name="nuevoEstado" required>
              <option value="">— Selecciona nuevo estado —</option>
              <% String est = pedido.getEstado();
                 if ("pendiente".equals(est)) { %>
                <option value="autorizado">✅ Autorizado (pago confirmado)</option>
                <option value="cancelado">❌ Cancelado</option>
              <% } else if ("autorizado".equals(est)) { %>
                <option value="preparado">📦 Preparado</option>
                <option value="cancelado">❌ Cancelado</option>
              <% } else if ("preparado".equals(est)) { %>
                <option value="en_camino">🚚 En Camino</option>
                <option value="cancelado">❌ Cancelado</option>
              <% } else if ("en_camino".equals(est)) { %>
                <option value="entregado">🎉 Entregado</option>
                <option value="cancelado">❌ Cancelado</option>
              <% } %>
            </select>
          </div>

          <div class="form-grupo">
            <label>Nota para el cliente (opcional)</label>
            <textarea name="notaAdmin" rows="3"
                      placeholder="Ej: Punto de encuentro: C.C. Pradera. Hora: 3pm"><%= pedido.getNotaAdmin() != null ? pedido.getNotaAdmin() : "" %></textarea>
            <small class="form-hint">Esta nota se enviará al cliente junto con la notificación.</small>
          </div>

          <button type="submit" class="btn-primario btn-grande btn-bloque"
                  onclick="return confirm('¿Cambiar el estado del pedido?')">
            Cambiar Estado + Notificar
          </button>
        </form>
        <% } else { %>
          <p class="texto-gris" style="margin-top:12px">
            Este pedido está <%= pedido.getEstadoLabel() %>. No se puede modificar.
          </p>
        <% } %>
      </div>

      <!-- Info entrega -->
      <div class="admin-card">
        <h3><%= Pedido.ENTREGA_DOMICILIO.equals(pedido.getTipoEntrega()) ? "🚚 Envío a Domicilio" : "📍 Entrega Personal" %></h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">Método de pago</span>
            <span><%= pedido.getTipoPago().substring(0,1).toUpperCase() + pedido.getTipoPago().substring(1) %></span>
          </div>
          <% if (pedido.getDireccion() != null && !pedido.getDireccion().isEmpty()) { %>
          <div class="info-item">
            <span class="info-label">Dirección</span>
            <span><%= pedido.getDireccion() %></span>
          </div>
          <% } %>
          <% if (pedido.getNotaAdmin() != null && !pedido.getNotaAdmin().isEmpty()) { %>
          <div class="info-item">
            <span class="info-label">Nota</span>
            <span><%= pedido.getNotaAdmin() %></span>
          </div>
          <% } %>
        </div>
      </div>

      <!-- WhatsApp -->
      <% if (pedido.getTelefonoCliente() != null && !pedido.getTelefonoCliente().isEmpty()) { %>
      <div class="admin-card">
        <h3>📱 Notificar por WhatsApp</h3>
        <p class="texto-gris">Envía una notificación manual al cliente sobre el estado de su pedido.</p>
        <form action="<%= ctx %>/admin/pedidos" method="post">
          <input type="hidden" name="accion"   value="enviarWA">
          <input type="hidden" name="pedidoId" value="<%= pedido.getId() %>">
          <button type="submit" class="btn-whatsapp btn-bloque">
            📱 Abrir WhatsApp con <%= pedido.getTelefonoCliente() %>
          </button>
        </form>
      </div>
      <% } %>

    </div>
  </div>
</div>
</body>
</html>
