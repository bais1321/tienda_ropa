<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,modelo.Producto,modelo.TallaProducto,modelo.ImagenProducto" %>
<%
    Producto p         = (Producto) request.getAttribute("producto");
    List<Producto> rel = (List<Producto>) request.getAttribute("relacionados");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= p.getNombre() %> &mdash; MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>

<%@ include file="/jsp/navbar.jsp" %>

<div class="contenedor detalle-layout">

  <div class="detalle-galeria">
    <div class="galeria-principal">
      <img id="imgPrincipal" src="<%= ctx %><%= p.getRutaImagenPrincipal() %>"
           alt="<%= p.getNombre() %>">
      <% if (p.tieneDescuento()) { %>
        <span class="badge-descuento badge-grande">-<%= p.getDescuento() %>%</span>
      <% } %>
    </div>
    <% if (p.getImagenes() != null && p.getImagenes().size() > 1) { %>
    <div class="galeria-thumbs">
      <% for (ImagenProducto img : p.getImagenes()) {
           String tSrc = ctx + img.getRutaImagen();
           String tSrcJs = tSrc.replace("'", "\\'");
           String tClass = img.getOrden() == 0 ? "thumb thumb-activa" : "thumb";
      %>
        <img src="<%= tSrc %>" alt="Vista <%= img.getOrden()+1 %>"
             class="<%= tClass %>"
             onclick="cambiarImagen(this,'<%= tSrcJs %>')">
      <% } %>
    </div>
    <% } %>
  </div>

  <div class="detalle-info">
    <p class="detalle-categoria">
      <a href="<%= ctx %>/catalogo?categoria=<%= p.getCategoria() %>"><%= p.getCategoria() %></a>
      /
      <a href="<%= ctx %>/catalogo?categoria=<%= p.getCategoria() %>&subcategoria=<%= p.getSubcategoria() %>"><%= p.getSubcategoria() %></a>
    </p>
    <h1 class="detalle-nombre"><%= p.getNombre() %></h1>
    <div class="detalle-precios">
      <% if (p.tieneDescuento()) { %>
        <span class="precio-tachado precio-tachado-lg">Q<%= p.getPrecioOriginal() %></span>
        <span class="precio-final precio-final-lg">Q<%= p.getPrecioFinal() %></span>
      <% } else { %>
        <span class="precio-final precio-final-lg">Q<%= p.getPrecioOriginal() %></span>
      <% } %>
    </div>
    <% if ("agotado".equals(p.getEstado())) { %>
      <p class="alerta-agotado">Este producto est&aacute; agotado temporalmente.</p>
    <% } else if (p.isStockBajo()) { %>
      <p class="alerta-stock-bajo">Solo quedan <strong><%= p.getStockTotal() %></strong> unidades.</p>
    <% } %>
    <p class="detalle-descripcion"><%= p.getDescripcion() %></p>

    <% if (!"agotado".equals(p.getEstado())) { %>
    <form action="<%= ctx %>/carrito" method="post" id="formCarrito">
      <input type="hidden" name="accion"     value="agregar">
      <input type="hidden" name="productoId" value="<%= p.getId() %>">
      <input type="hidden" name="tallaId"    id="tallaIdSeleccionada" value="">
      <div class="detalle-seccion">
        <div class="talla-header">
          <h4>Talla</h4>
          <span id="stockTallaTexto" class="stock-talla-info"></span>
        </div>
        <div class="tallas-grid">
          <% for (TallaProducto t : p.getTallas()) { %>
            <button type="button"
                    class="btn-talla <%= t.isDisponible() ? "" : "btn-talla-agotada" %>"
                    data-talla-id="<%= t.getId() %>"
                    data-stock="<%= t.getStock() %>"
                    <%= !t.isDisponible() ? "disabled" : "" %>
                    onclick="seleccionarTalla(this)">
              <%= t.getTalla() %>
              <% if (!t.isDisponible()) { %><span class="talla-x">X</span><% } %>
            </button>
          <% } %>
        </div>
        <p id="errorTalla" class="error-talla oculto">Por favor selecciona una talla.</p>
      </div>
      <div class="detalle-seccion">
        <h4>Cantidad</h4>
        <div class="cantidad-control">
          <button type="button" onclick="cambiarCantidad(-1)">&#8722;</button>
          <input type="number" name="cantidad" id="inputCantidad" value="1" min="1" max="1" readonly>
          <button type="button" onclick="cambiarCantidad(1)">+</button>
        </div>
      </div>
      <button type="button" class="btn-primario btn-grande btn-bloque"
              onclick="validarYAgregar()">
        Agregar al carrito
      </button>
    </form>
    <% } else { %>
      <button class="btn-primario btn-grande btn-bloque btn-deshabilitado" disabled>Producto Agotado</button>
    <% } %>

    <div class="detalle-extras">
      <div class="extra-item"><span>&#128666;</span> Env&iacute;o a domicilio Q20 (compra m&iacute;nima Q100)</div>
      <div class="extra-item"><span>&#128205;</span> Entrega personal disponible</div>
      <div class="extra-item"><span>&#128179;</span> Efectivo o transferencia</div>
    </div>
  </div>
</div>

<% if (rel != null && !rel.isEmpty()) { %>
<section class="seccion-productos seccion-relacionados">
  <div class="contenedor">
    <h2>Tambi&eacute;n te puede gustar</h2>
    <div class="productos-grid">
      <% for (Producto r : rel) { %>
      <div class="tarjeta-producto">
        <a href="<%= ctx %>/producto?id=<%= r.getId() %>" class="tarjeta-link">
          <div class="tarjeta-imagen">
            <img src="<%= ctx %><%= r.getRutaImagenPrincipal() %>" alt="<%= r.getNombre() %>" loading="lazy">
            <% if (r.tieneDescuento()) { %><span class="badge-descuento">-<%= r.getDescuento() %>%</span><% } %>
          </div>
          <div class="tarjeta-info">
            <h3 class="tarjeta-nombre"><%= r.getNombre() %></h3>
            <div class="tarjeta-precios">
              <% if (r.tieneDescuento()) { %>
                <span class="precio-tachado">Q<%= r.getPrecioOriginal() %></span>
                <span class="precio-final">Q<%= r.getPrecioFinal() %></span>
              <% } else { %>
                <span class="precio-final">Q<%= r.getPrecioOriginal() %></span>
              <% } %>
            </div>
          </div>
        </a>
      </div>
      <% } %>
    </div>
  </div>
</section>
<% } %>

<%@ include file="/jsp/footer.jsp" %>
<script>
var stockTallaActual = 0;
function seleccionarTalla(btn) {
  document.querySelectorAll('.btn-talla').forEach(function(b){ b.classList.remove('btn-talla-activa'); });
  btn.classList.add('btn-talla-activa');
  document.getElementById('tallaIdSeleccionada').value = btn.dataset.tallaId;
  stockTallaActual = parseInt(btn.dataset.stock);
  var inputCant = document.getElementById('inputCantidad');
  inputCant.max = stockTallaActual;
  if (parseInt(inputCant.value) > stockTallaActual) inputCant.value = stockTallaActual;
  var info = document.getElementById('stockTallaTexto');
  if (stockTallaActual <= 4) {
    info.textContent = 'Solo quedan ' + stockTallaActual + ' unidades';
    info.className = 'stock-talla-info stock-bajo';
  } else {
    info.textContent = 'Disponibles: ' + stockTallaActual;
    info.className = 'stock-talla-info';
  }
  document.getElementById('errorTalla').classList.add('oculto');
}
function cambiarCantidad(delta) {
  var input = document.getElementById('inputCantidad');
  var nueva = parseInt(input.value) + delta;
  var max   = stockTallaActual > 0 ? stockTallaActual : 1;
  if (nueva >= 1 && nueva <= max) input.value = nueva;
}
function validarYAgregar() {
  if (!document.getElementById('tallaIdSeleccionada').value) {
    document.getElementById('errorTalla').classList.remove('oculto');
    return;
  }
  document.getElementById('formCarrito').submit();
}
function cambiarImagen(thumb, src) {
  document.getElementById('imgPrincipal').src = src;
  document.querySelectorAll('.thumb').forEach(function(t){ t.classList.remove('thumb-activa'); });
  thumb.classList.add('thumb-activa');
}
</script>
<script src="<%= ctx %>/js/carrito.js"></script>
</body>
</html>
