<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="modelo.Pedido" %>
<%@ page import="modelo.ItemCarrito" %>
<%
    /*
     * SOLUCIÓN DEFINITIVA:
     * ConfigTienda NO se castea aquí. El ConfirmacionServlet ya extrajo
     * linkWhatsapp como String simple antes de enviarlo al JSP.
     * El JSP solo trabaja con tipos básicos: String, BigDecimal, Pedido, List.
     */
    Pedido pedido           = (Pedido)           request.getAttribute("pedido");
    List<ItemCarrito> items = (List<ItemCarrito>) request.getAttribute("itemsResumen");
    String linkWA           = (String)            request.getAttribute("linkWhatsapp");
    String ctx              = request.getContextPath();

    // Guardia: si no hay pedido redirigir al inicio
    if (pedido == null) {
        response.sendRedirect(ctx + "/");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>¡Pedido Confirmado! — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>

<%@ include file="/jsp/navbar.jsp" %>

<div class="contenedor pagina-confirmacion">

  <div class="confirmacion-header">
    <div class="check-animado">✓</div>
    <h1>¡Pedido Recibido!</h1>
    <p>Hemos recibido tu pedido. Te notificaremos por correo sobre el estado.</p>
    <div class="codigo-pedido-grande">
      <span class="codigo-label">Código de tu pedido:</span>
      <span class="codigo-valor"><%= pedido.getCodigoPedido() %></span>
    </div>
    <p class="codigo-guardar">📌 Guarda este código para rastrear tu pedido.</p>
  </div>

  <div class="confirmacion-layout">
    <div class="confirmacion-detalles">
      <h3>Detalles del Pedido</h3>
      <% if (items != null) {
           for (ItemCarrito it : items) { %>
      <div class="resumen-item">
        <img src="<%= ctx %><%= it.getImagenPrincipal() %>" alt="">
        <div>
          <p><strong><%= it.getNombreProducto() %></strong></p>
          <p>Talla: <%= it.getTalla() %> · Cantidad: <%= it.getCantidad() %></p>
        </div>
        <span>Q<%= it.getSubtotalLinea() %></span>
      </div>
      <% } } %>

      <div class="resumen-totales">
        <div class="resumen-linea">
          <span>Subtotal</span><span>Q<%= pedido.getSubtotal() %></span>
        </div>
        <% if (pedido.getDescuentoPrimeraCompra().compareTo(BigDecimal.ZERO) > 0) { %>
        <div class="resumen-linea resumen-descuento">
          <span>Descuento primera compra</span>
          <span>-Q<%= pedido.getDescuentoPrimeraCompra() %></span>
        </div>
        <% } %>
        <div class="resumen-linea">
          <span>Envío</span>
          <span><%= pedido.getCostoEnvio().compareTo(BigDecimal.ZERO) > 0
                    ? "Q" + pedido.getCostoEnvio() : "Gratis" %></span>
        </div>
        <div class="resumen-total">
          <span>Total Pagado</span>
          <span class="precio-final">Q<%= pedido.getTotal() %></span>
        </div>
      </div>
    </div>

    <div class="confirmacion-acciones">
      <h3>¿Qué sigue?</h3>

      <% if (pedido.esPorTransferencia() && linkWA != null) { %>
      <div class="accion-card accion-wa">
        <h4>📸 Envía tu comprobante</h4>
        <p>Realiza la transferencia con los datos bancarios que te enviamos al correo
           y envía el comprobante para confirmar tu pedido.</p>
        <a href="<%= linkWA %>" target="_blank" class="btn-whatsapp">
          📱 Enviar comprobante por WhatsApp
        </a>
      </div>
      <% } else if (!pedido.esPorTransferencia()) { %>
      <div class="accion-card">
        <h4>💵 Pago en efectivo</h4>
        <p>Ten el monto exacto listo al momento de la entrega. Te contactaremos para coordinar.</p>
      </div>
      <% } %>

      <div class="accion-card">
        <h4>📦 Rastrea tu pedido</h4>
        <p>Usa tu código y correo para ver el estado en cualquier momento.</p>
        <a href="<%= ctx %>/rastreo?codigo=<%= pedido.getCodigoPedido() %>&email=<%= pedido.getEmailCliente() %>"
           class="btn-secundario btn-bloque">Rastrear mi pedido</a>
      </div>

      <% if (pedido.esDeInvitado()) {
           String telCliente = pedido.getTelefonoCliente() != null
                               ? pedido.getTelefonoCliente() : "";
      %>
      <div class="accion-card accion-registro">
        <h4>🎉 Crea tu cuenta</h4>
        <p>Guarda tu historial y obtén el <strong>5% de descuento</strong> en tu próxima compra.</p>
        <a href="<%= ctx %>/crear-cuenta?nombre=<%= pedido.getNombreCliente() %>&apellido=<%= pedido.getApellidoCliente() %>&email=<%= pedido.getEmailCliente() %>&telefono=<%= telCliente %>"
           class="btn-primario btn-bloque">Crear mi cuenta gratis →</a>
      </div>
      <% } %>

      <a href="<%= ctx %>/" class="btn-link-centrado">← Volver al inicio</a>
    </div>
  </div>
</div>

<%@ include file="/jsp/footer.jsp" %>
<script src="<%= ctx %>/js/carrito.js"></script>
</body>
</html>
