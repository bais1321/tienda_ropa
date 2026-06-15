<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, modelo.ItemCarrito, java.math.BigDecimal" %>
<%
    String ctx = request.getContextPath();
    List<ItemCarrito> items = (List<ItemCarrito>) request.getAttribute("items");
    BigDecimal subtotal = BigDecimal.ZERO;
    if (items != null) for (ItemCarrito it : items) subtotal = subtotal.add(it.getSubtotalLinea());
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Carrito — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>
<%@ include file="/jsp/navbar.jsp" %>
<div class="contenedor pagina-carrito">
  <h1 class="pagina-titulo">&#128722; Carrito de Compras</h1>
  <% if (items == null || items.isEmpty()) { %>
    <div class="estado-vacio">
      <span>&#128722;</span>
      <h3>Tu carrito está vacío</h3>
      <p>Agrega productos desde el catálogo.</p>
      <a href="<%= ctx %>/catalogo" class="btn-primario">Ir al catálogo</a>
    </div>
  <% } else { %>
  <div class="carrito-layout">
    <div class="carrito-items">
      <% for (ItemCarrito item : items) { %>
      <div class="carrito-item">
        <img src="<%= ctx %><%= item.getImagenPrincipal() %>" alt="" class="item-imagen">
        <div class="item-info">
          <h4><%= item.getNombreProducto() %></h4>
          <p class="item-talla">Talla: <strong><%= item.getTalla() %></strong></p>
          <% if (item.getDescuentoAplicado() > 0) { %><p class="item-descuento">-<%= item.getDescuentoAplicado() %>% aplicado</p><% } %>
        </div>
        <div class="item-controles">
          <form action="<%= ctx %>/carrito" method="post" class="form-cantidad">
            <input type="hidden" name="accion" value="actualizar">
            <input type="hidden" name="clave"  value="<%= item.getClave() %>">
            <div class="cantidad-control cantidad-sm">
              <button type="submit" name="cantidad" value="<%= item.getCantidad() - 1 %>">−</button>
              <span><%= item.getCantidad() %></span>
              <button type="submit" name="cantidad" value="<%= item.getCantidad() + 1 %>"
                      <%= item.getCantidad() >= item.getStockDisponible() ? "disabled" : "" %>>+</button>
            </div>
          </form>
          <p class="item-subtotal">Q<%= item.getSubtotalLinea() %></p>
          <form action="<%= ctx %>/carrito" method="post">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="clave"  value="<%= item.getClave() %>">
            <button type="submit" class="btn-eliminar-item">✕</button>
          </form>
        </div>
      </div>
      <% } %>
      <div class="carrito-acciones-bottom">
        <form action="<%= ctx %>/carrito" method="post">
          <input type="hidden" name="accion" value="vaciar">
          <button type="submit" class="btn-link-peligro" onclick="return confirm('¿Vaciar el carrito?')">&#128465; Vaciar carrito</button>
        </form>
        <a href="<%= ctx %>/catalogo" class="btn-secundario">← Seguir comprando</a>
      </div>
    </div>
    <div class="carrito-resumen">
      <h3>Resumen del Pedido</h3>
      <div class="resumen-linea"><span>Subtotal</span><span>Q<%= subtotal %></span></div>
      <div class="resumen-linea resumen-envio"><span>Envío</span><span class="texto-gris">Se calcula al pagar</span></div>
      <div class="resumen-total"><span>Total estimado</span><span class="precio-final">Q<%= subtotal %></span></div>
      <p class="resumen-nota">* El total final incluirá el costo de envío si aplica.</p>
      <a href="<%= ctx %>/checkout" class="btn-primario btn-grande btn-bloque">Proceder al pago →</a>
    </div>
  </div>
  <% } %>
</div>
<%@ include file="/jsp/footer.jsp" %>
<script src="<%= ctx %>/js/carrito.js"></script>
</body>
</html>
