<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*, modelo.Pedido, modelo.Producto, java.math.BigDecimal" %>
<%
    String ctx = request.getContextPath();
    Map<String,Object> kpis    = (Map<String,Object>) request.getAttribute("kpis");
    List<Pedido>   pedidosRec  = (List<Pedido>)   request.getAttribute("pedidosRecientes");
    List<Producto> stockBajo   = (List<Producto>) request.getAttribute("stockBajo");
    if (kpis == null) kpis = new java.util.HashMap<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Dashboard — Admin MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
  <link rel="stylesheet" href="<%= ctx %>/css/admin.css">
</head>
<body class="admin-body">
<%@ include file="/jsp/admin/navbar_admin.jsp" %>
<div class="admin-contenedor">
  <h1 class="admin-titulo">&#128202; Dashboard</h1>

  <div class="kpi-grid">
    <div class="kpi-card">
      <div class="kpi-icono kpi-amarillo">&#128176;</div>
      <div class="kpi-datos">
        <span class="kpi-valor">Q<%= kpis.getOrDefault("ventasHoy","0.00") %></span>
        <span class="kpi-label">Ventas Hoy</span>
      </div>
    </div>
    <div class="kpi-card">
      <div class="kpi-icono kpi-azul">&#128230;</div>
      <div class="kpi-datos">
        <span class="kpi-valor"><%= kpis.getOrDefault("totalPedidosHoy",0) %></span>
        <span class="kpi-label">Pedidos Hoy</span>
      </div>
    </div>
    <div class="kpi-card kpi-alerta">
      <div class="kpi-icono kpi-naranja">&#9203;</div>
      <div class="kpi-datos">
        <span class="kpi-valor"><%= kpis.getOrDefault("pendientes",0) %></span>
        <span class="kpi-label">Pendientes</span>
      </div>
    </div>
    <div class="kpi-card">
      <div class="kpi-icono kpi-verde">&#10003;</div>
      <div class="kpi-datos">
        <span class="kpi-valor"><%= kpis.getOrDefault("entregados",0) %></span>
        <span class="kpi-label">Entregados Hoy</span>
      </div>
    </div>
  </div>

  <div class="estados-resumen">
    <a href="<%= ctx %>/admin/pedidos?estado=autorizado"  class="estado-mini-card"><span class="badge-estado badge-azul">Autorizados</span><span class="estado-mini-num"><%= kpis.getOrDefault("autorizados",0) %></span></a>
    <a href="<%= ctx %>/admin/pedidos?estado=preparado"   class="estado-mini-card"><span class="badge-estado badge-amarillo">Preparados</span><span class="estado-mini-num"><%= kpis.getOrDefault("preparados",0) %></span></a>
    <a href="<%= ctx %>/admin/pedidos?estado=en_camino"   class="estado-mini-card"><span class="badge-estado badge-morado">En Camino</span><span class="estado-mini-num"><%= kpis.getOrDefault("enCamino",0) %></span></a>
    <a href="<%= ctx %>/admin/pedidos?estado=cancelado"   class="estado-mini-card"><span class="badge-estado badge-rojo">Cancelados</span><span class="estado-mini-num"><%= kpis.getOrDefault("cancelados",0) %></span></a>
  </div>

  <div class="admin-dos-col">
    <div class="admin-card">
      <div class="admin-card-header">
        <h3>&#9203; Pedidos Pendientes</h3>
        <a href="<%= ctx %>/admin/pedidos?estado=pendiente" class="link-ver-todos">Ver todos →</a>
      </div>
      <% if (pedidosRec == null || pedidosRec.isEmpty()) { %>
        <p class="sin-datos">No hay pedidos pendientes. &#10003;</p>
      <% } else { %>
      <table class="admin-tabla">
        <thead><tr><th>Código</th><th>Cliente</th><th>Total</th><th>Pago</th><th></th></tr></thead>
        <tbody>
          <% for (Pedido p : pedidosRec) { %>
          <tr>
            <td><strong><%= p.getCodigoPedido() %></strong></td>
            <td><%= p.getNombreCompletoCliente() %></td>
            <td>Q<%= p.getTotal() %></td>
            <td><span class="badge-pago"><%= p.getTipoPago() %></span></td>
            <td><a href="<%= ctx %>/admin/pedidos?accion=ver&id=<%= p.getId() %>" class="btn-sm btn-primario">Ver</a></td>
          </tr>
          <% } %>
        </tbody>
      </table>
      <% } %>
    </div>

    <div class="admin-card">
      <div class="admin-card-header">
        <h3>&#9888; Stock Bajo</h3>
        <a href="<%= ctx %>/admin/productos" class="link-ver-todos">Ver productos →</a>
      </div>
      <% if (stockBajo == null || stockBajo.isEmpty()) { %>
        <p class="sin-datos">Todos los productos tienen stock suficiente. &#10003;</p>
      <% } else { %>
      <table class="admin-tabla">
        <thead><tr><th>Producto</th><th>Categoría</th><th>Stock</th><th></th></tr></thead>
        <tbody>
          <% for (Producto prod : stockBajo) { %>
          <tr>
            <td><strong><%= prod.getNombre() %></strong></td>
            <td><%= prod.getCategoria() %></td>
            <td><span class="stock-alerta"><%= prod.getStockTotal() %></span></td>
            <td><a href="<%= ctx %>/admin/productos?accion=editar&id=<%= prod.getId() %>" class="btn-sm btn-secundario">Editar</a></td>
          </tr>
          <% } %>
        </tbody>
      </table>
      <% } %>
    </div>
  </div>
</div>
</body>
</html>
