<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="modelo.Pedido, modelo.DetallePedido, java.util.List" %>
<%
    String ctx           = request.getContextPath();
    Pedido pedido        = (Pedido) request.getAttribute("pedido");
    String errorMsg      = (String) request.getAttribute("error");
    String codIngresado  = (String) request.getAttribute("codigoIngresado");
    String emailIngresado= (String) request.getAttribute("emailIngresado");
    if (codIngresado   == null) codIngresado   = "";
    if (emailIngresado == null) emailIngresado = "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Rastrear Pedido — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>
<%@ include file="/jsp/navbar.jsp" %>
<div class="contenedor pagina-rastreo">
  <h1 class="pagina-titulo">&#128230; Rastrear Pedido</h1>
  <div class="rastreo-form-card">
    <form action="<%= ctx %>/rastreo" method="post">
      <% if (errorMsg != null) { %><div class="alerta alerta-error"><%= errorMsg %></div><% } %>
      <div class="form-grid-2">
        <div class="form-grupo">
          <label>Código de Pedido</label>
          <input type="text" name="codigo" placeholder="Ej: E-00123 o T-00456" required
                 value="<%= codIngresado %>" style="text-transform:uppercase">
        </div>
        <div class="form-grupo">
          <label>Correo Electrónico</label>
          <input type="email" name="email" placeholder="El correo con que compraste" required
                 value="<%= emailIngresado %>">
        </div>
      </div>
      <button type="submit" class="btn-primario">Buscar Pedido</button>
    </form>
  </div>

  <% if (pedido != null) { %>
  <div class="rastreo-resultado">
    <div class="rastreo-header">
      <div>
        <h2>Pedido <span class="codigo-inline"><%= pedido.getCodigoPedido() %></span></h2>
        <p>Realizado el <%= pedido.getFecha() %></p>
      </div>
      <span class="badge-estado badge-<%= pedido.getEstado() %>"><%= pedido.getEstadoLabel() %></span>
    </div>

    <div class="estado-timeline">
      <%
        String[] estArr  = {"pendiente","autorizado","preparado","en_camino","entregado"};
        String[] labArr  = {"Pendiente","Autorizado","Preparado","En camino","Entregado"};
        String[] icoArr  = {"&#9200;","&#10003;","&#128230;","&#128666;","&#127881;"};
        boolean alcanzado = !"cancelado".equals(pedido.getEstado());
        for (int i = 0; i < estArr.length; i++) {
          boolean activo = pedido.getEstado().equals(estArr[i]);
          boolean pasado = alcanzado && !activo;
          if (activo) alcanzado = false;
      %>
      <div class="tl-item <%= activo ? "tl-activo" : pasado ? "tl-pasado" : "" %>">
        <div class="tl-icono"><%= icoArr[i] %></div>
        <span><%= labArr[i] %></span>
      </div>
      <% if (i < estArr.length - 1) { %><div class="tl-linea"></div><% } %>
      <% } %>
      <% if ("cancelado".equals(pedido.getEstado())) { %>
        <div class="tl-linea"></div>
        <div class="tl-item tl-activo tl-cancelado">
          <div class="tl-icono">&#10060;</div><span>Cancelado</span>
        </div>
      <% } %>
    </div>

    <% if (pedido.getNotaAdmin() != null && !pedido.getNotaAdmin().isEmpty()) { %>
    <div class="nota-entrega">
      <h4>&#128205; Información de entrega:</h4>
      <p><%= pedido.getNotaAdmin() %></p>
    </div>
    <% } %>

    <div class="rastreo-detalles-grid">
      <div><h4>Cliente</h4><p><%= pedido.getNombreCompletoCliente() %></p><p><%= pedido.getEmailCliente() %></p></div>
      <div><h4>Entrega</h4><p><%= Pedido.ENTREGA_DOMICILIO.equals(pedido.getTipoEntrega()) ? "A domicilio" : "Entrega personal" %></p><% if (pedido.getDireccion() != null) { %><p><%= pedido.getDireccion() %></p><% } %></div>
      <div><h4>Pago</h4><p><%= pedido.getTipoPago().substring(0,1).toUpperCase() + pedido.getTipoPago().substring(1) %></p><p class="precio-final">Q<%= pedido.getTotal() %></p></div>
    </div>

    <% List<DetallePedido> detalles = pedido.getDetalles();
       if (detalles != null && !detalles.isEmpty()) { %>
    <div class="rastreo-productos">
      <h4>Productos</h4>
      <% for (DetallePedido d : detalles) {
           String imgRastreo = d.getImagenProducto() != null
                               ? ctx + d.getImagenProducto()
                               : ctx + "/img/placeholder.jpg";
      %>
      <div class="resumen-item">
        <img src="<%= imgRastreo %>" alt="">
        <div><p><strong><%= d.getNombreProducto() %></strong></p><p>Talla: <%= d.getTalla() %> · Cantidad: <%= d.getCantidad() %></p></div>
        <span>Q<%= d.getSubtotalLinea() %></span>
      </div>
      <% } %>
    </div>
    <% } %>
  </div>
  <% } %>
</div>
<%@ include file="/jsp/footer.jsp" %>
</body>
</html>
