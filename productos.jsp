<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, modelo.Producto" %>
<%
    String ctx = request.getContextPath();
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Productos — Admin MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
  <link rel="stylesheet" href="<%= ctx %>/css/admin.css">
</head>
<body class="admin-body">
<%@ include file="/jsp/admin/navbar_admin.jsp" %>
<div class="admin-contenedor">
  <div class="admin-page-header">
    <h1 class="admin-titulo">&#128247; Productos</h1>
    <a href="<%= ctx %>/admin/productos?accion=nuevo" class="btn-primario">+ Nuevo Producto</a>
  </div>
  <div class="admin-filtros-rapidos">
    <a href="<%= ctx %>/admin/productos" class="btn-filtro-rapido activo">Todos</a>
  </div>
  <div class="admin-card">
    <% if (productos == null || productos.isEmpty()) { %>
      <p class="sin-datos">No hay productos registrados.</p>
    <% } else { %>
    <table class="admin-tabla admin-tabla-full">
      <thead>
        <tr><th>Imagen</th><th>Nombre</th><th>Categoría</th><th>Precio</th><th>Descuento</th><th>Stock</th><th>Estado</th><th>Acciones</th></tr>
      </thead>
      <tbody>
        <% for (Producto p : productos) { %>
        <tr>
          <td><img src="<%= ctx %><%= p.getRutaImagenPrincipal() %>" alt="" class="tabla-img"></td>
          <td><strong><%= p.getNombre() %></strong></td>
          <td><%= p.getCategoria() %> / <%= p.getSubcategoria() %></td>
          <td>Q<%= p.getPrecioOriginal() %></td>
          <td><% if (p.tieneDescuento()) { %><span class="badge-descuento"><%= p.getDescuento() %>%</span><% } else { %>—<% } %></td>
          <td><span class="<%= p.getStockTotal() <= 4 ? "stock-alerta" : "" %>"><%= p.getStockTotal() %></span></td>
          <td><span class="badge-estado badge-<%= p.getEstado() %>"><%= p.getEstado() %></span></td>
          <td class="acciones-col">
            <a href="<%= ctx %>/admin/productos?accion=editar&id=<%= p.getId() %>" class="btn-sm btn-secundario">Editar</a>
            <form action="<%= ctx %>/admin/productos" method="post" style="display:inline">
              <input type="hidden" name="accion" value="cambiarEstado">
              <input type="hidden" name="id"     value="<%= p.getId() %>">
              <% if ("activo".equals(p.getEstado())) { %>
                <input type="hidden" name="estado" value="inactivo">
                <button type="submit" class="btn-sm btn-peligro">Ocultar</button>
              <% } else if ("inactivo".equals(p.getEstado())) { %>
                <input type="hidden" name="estado" value="activo">
                <button type="submit" class="btn-sm btn-verde">Activar</button>
              <% } %>
            </form>
          </td>
        </tr>
        <% } %>
      </tbody>
    </table>
    <% } %>
  </div>
</div>
</body>
</html>
