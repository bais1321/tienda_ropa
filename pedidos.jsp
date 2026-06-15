<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, modelo.Pedido" %>
<%
    String ctx = request.getContextPath();
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    String estadoFiltro  = (String) request.getAttribute("estadoFiltro");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Pedidos — Admin MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
  <link rel="stylesheet" href="<%= ctx %>/css/admin.css">
</head>
<body class="admin-body">
<%@ include file="/jsp/admin/navbar_admin.jsp" %>
<div class="admin-contenedor">
  <h1 class="admin-titulo">&#128230; Pedidos</h1>
  <div class="admin-filtros-rapidos">
    <a href="<%= ctx %>/admin/pedidos" class="btn-filtro-rapido <%= estadoFiltro == null ? "activo" : "" %>">Todos</a>
    <a href="<%= ctx %>/admin/pedidos?estado=pendiente"   class="btn-filtro-rapido <%= "pendiente".equals(estadoFiltro)  ? "activo" : "" %>">Pendientes</a>
    <a href="<%= ctx %>/admin/pedidos?estado=autorizado"  class="btn-filtro-rapido <%= "autorizado".equals(estadoFiltro) ? "activo" : "" %>">Autorizados</a>
    <a href="<%= ctx %>/admin/pedidos?estado=preparado"   class="btn-filtro-rapido <%= "preparado".equals(estadoFiltro)  ? "activo" : "" %>">Preparados</a>
    <a href="<%= ctx %>/admin/pedidos?estado=en_camino"   class="btn-filtro-rapido <%= "en_camino".equals(estadoFiltro)  ? "activo" : "" %>">En Camino</a>
    <a href="<%= ctx %>/admin/pedidos?estado=entregado"   class="btn-filtro-rapido <%= "entregado".equals(estadoFiltro)  ? "activo" : "" %>">Entregados</a>
    <a href="<%= ctx %>/admin/pedidos?estado=cancelado"   class="btn-filtro-rapido <%= "cancelado".equals(estadoFiltro)  ? "activo" : "" %>">Cancelados</a>
  </div>
  <div class="admin-card">
    <% if (pedidos == null || pedidos.isEmpty()) { %>
      <p class="sin-datos">No hay pedidos con este filtro.</p>
    <% } else { %>
    <table class="admin-tabla admin-tabla-full">
      <thead>
        <tr><th>Código</th><th>Cliente</th><th>Total</th><th>Pago</th><th>Entrega</th><th>Fecha</th><th>Estado</th><th></th></tr>
      </thead>
      <tbody>
        <% for (Pedido p : pedidos) { %>
        <tr>
          <td><strong><%= p.getCodigoPedido() %></strong></td>
          <td><%= p.getNombreCompletoCliente() %><br><small class="texto-gris"><%= p.getEmailCliente() %></small></td>
          <td><strong>Q<%= p.getTotal() %></strong></td>
          <td><span class="badge-pago"><%= p.getTipoPago() %></span></td>
          <td><%= Pedido.ENTREGA_DOMICILIO.equals(p.getTipoEntrega()) ? "&#128666; Domicilio" : "&#128205; Personal" %></td>
          <td><small><%= p.getFecha() %></small></td>
          <td><span class="badge-estado badge-<%= p.getEstado() %>"><%= p.getEstadoLabel() %></span></td>
          <td><a href="<%= ctx %>/admin/pedidos?accion=ver&id=<%= p.getId() %>" class="btn-sm btn-primario">Ver →</a></td>
        </tr>
        <% } %>
      </tbody>
    </table>
    <% } %>
  </div>
</div>
</body>
</html>
