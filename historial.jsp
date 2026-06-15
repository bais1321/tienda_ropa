<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, modelo.Pedido, modelo.Usuario" %>
<%
    String ctx       = request.getContextPath();
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    Usuario u = (Usuario) session.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Mis Pedidos — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>
<%@ include file="/jsp/navbar.jsp" %>
<div class="contenedor pagina-historial">
  <h1 class="pagina-titulo">&#128230; Mis Pedidos</h1>
  <p class="pagina-subtitulo">Hola, <strong><%= u != null ? u.getNombre() : "" %></strong>.</p>
  <% if (pedidos == null || pedidos.isEmpty()) { %>
    <div class="estado-vacio">
      <span>&#128230;</span>
      <h3>Aún no has realizado pedidos</h3>
      <p>¡Explora el catálogo y realiza tu primera compra!</p>
      <a href="<%= ctx %>/catalogo" class="btn-primario">Ir al catálogo</a>
    </div>
  <% } else { %>
    <div class="historial-lista">
      <% for (Pedido p : pedidos) { %>
      <div class="historial-card">
        <div class="historial-card-header">
          <div>
            <span class="historial-codigo"><%= p.getCodigoPedido() %></span>
            <span class="historial-fecha"><%= p.getFecha() %></span>
          </div>
          <span class="badge-estado badge-<%= p.getEstado() %>"><%= p.getEstadoLabel() %></span>
        </div>
        <div class="historial-card-body">
          <div class="historial-dato"><span>Entrega</span><strong><%= Pedido.ENTREGA_DOMICILIO.equals(p.getTipoEntrega()) ? "A domicilio" : "Personal" %></strong></div>
          <div class="historial-dato"><span>Pago</span><strong><%= p.getTipoPago() %></strong></div>
          <div class="historial-dato"><span>Total</span><strong class="precio-final">Q<%= p.getTotal() %></strong></div>
        </div>
        <div class="historial-card-footer">
          <a href="<%= ctx %>/rastreo?codigo=<%= p.getCodigoPedido() %>&email=<%= p.getEmailCliente() %>" class="btn-secundario btn-sm">Rastrear pedido →</a>
        </div>
      </div>
      <% } %>
    </div>
  <% } %>
</div>
<%@ include file="/jsp/footer.jsp" %>
</body>
</html>
