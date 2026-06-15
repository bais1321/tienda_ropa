<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,modelo.Producto" %>
<%
    List<Producto> destacados = (List<Producto>) request.getAttribute("destacados");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MiTienda &mdash; Ropa y Zapatos</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>

<%@ include file="/jsp/navbar.jsp" %>

<section class="hero">
  <div class="hero-contenido">
    <span class="hero-tag">Nueva Colecci&oacute;n 2026</span>
    <h1>Estilo que<br><span class="amarillo">habla por ti</span></h1>
    <p>Ropa y zapatos para hombre y mujer. Entrega personal o a domicilio.</p>
    <div class="hero-botones">
      <a href="<%= ctx %>/catalogo?categoria=ropa"    class="btn-primario">Ver Ropa</a>
      <a href="<%= ctx %>/catalogo?categoria=zapatos" class="btn-secundario">Ver Zapatos</a>
    </div>
  </div>
  <div class="hero-imagen">
    <div class="hero-img-placeholder"><span>&#128705;</span></div>
  </div>
</section>

<section class="categorias">
  <div class="contenedor">
    <div class="cats-grid">
      <a href="<%= ctx %>/catalogo?categoria=ropa&subcategoria=hombre" class="cat-card">
        <div class="cat-icono">&#128084;</div><h3>Ropa Hombre</h3><span>Ver colecci&oacute;n &rarr;</span>
      </a>
      <a href="<%= ctx %>/catalogo?categoria=ropa&subcategoria=mujer" class="cat-card">
        <div class="cat-icono">&#128149;</div><h3>Ropa Mujer</h3><span>Ver colecci&oacute;n &rarr;</span>
      </a>
      <a href="<%= ctx %>/catalogo?categoria=zapatos&subcategoria=hombre" class="cat-card">
        <div class="cat-icono">&#128094;</div><h3>Zapatos Hombre</h3><span>Ver colecci&oacute;n &rarr;</span>
      </a>
      <a href="<%= ctx %>/catalogo?categoria=zapatos&subcategoria=mujer" class="cat-card">
        <div class="cat-icono">&#128160;</div><h3>Zapatos Mujer</h3><span>Ver colecci&oacute;n &rarr;</span>
      </a>
    </div>
  </div>
</section>

<% if (destacados != null && !destacados.isEmpty()) { %>
<section class="seccion-productos">
  <div class="contenedor">
    <div class="seccion-header">
      <h2>Productos Destacados</h2>
      <a href="<%= ctx %>/catalogo" class="link-ver-todos">Ver todos &rarr;</a>
    </div>
    <div class="productos-grid">
      <% for (Producto p : destacados) {
           String imgSrc = ctx + p.getRutaImagenPrincipal();
           String estado = p.getEstado();
      %>
      <div class="tarjeta-producto">
        <a href="<%= ctx %>/producto?id=<%= p.getId() %>" class="tarjeta-link">
          <div class="tarjeta-imagen">
            <img src="<%= imgSrc %>" alt="<%= p.getNombre() %>" loading="lazy">
            <% if (p.tieneDescuento()) { %><span class="badge-descuento">-<%= p.getDescuento() %>%</span><% } %>
            <% if ("agotado".equals(estado)) { %><div class="overlay-agotado">Agotado</div><% } %>
          </div>
          <div class="tarjeta-info">
            <p class="tarjeta-categoria"><%= p.getCategoria() %> &middot; <%= p.getSubcategoria() %></p>
            <h3 class="tarjeta-nombre"><%= p.getNombre() %></h3>
            <div class="tarjeta-precios">
              <% if (p.tieneDescuento()) { %>
                <span class="precio-tachado">Q<%= p.getPrecioOriginal() %></span>
                <span class="precio-final">Q<%= p.getPrecioFinal() %></span>
              <% } else { %>
                <span class="precio-final">Q<%= p.getPrecioOriginal() %></span>
              <% } %>
            </div>
          </div>
        </a>
        <% if (!"agotado".equals(estado)) { %>
          <a href="<%= ctx %>/producto?id=<%= p.getId() %>" class="btn-agregar-rapido">Ver producto</a>
        <% } else { %>
          <button class="btn-agregar-rapido btn-deshabilitado" disabled>Agotado</button>
        <% } %>
      </div>
      <% } %>
    </div>
  </div>
</section>
<% } %>

<section class="banner-descuento">
  <div class="contenedor">
    <div class="banner-inner">
      <div>
        <h2>5% de descuento en tu primera compra</h2>
        <p>Crea tu cuenta hoy y tienes 10 d&iacute;as para usar tu descuento exclusivo.</p>
      </div>
      <a href="<%= ctx %>/registro" class="btn-primario">Crear cuenta gratis</a>
    </div>
  </div>
</section>

<section class="beneficios">
  <div class="contenedor">
    <div class="beneficios-grid">
      <div class="beneficio"><span class="b-icono">&#128666;</span><h4>Env&iacute;o a Domicilio</h4><p>Llevamos tu pedido hasta tu puerta por solo Q20.</p></div>
      <div class="beneficio"><span class="b-icono">&#128205;</span><h4>Entrega Personal</h4><p>Coordina un punto de encuentro y recoge tu pedido.</p></div>
      <div class="beneficio"><span class="b-icono">&#128179;</span><h4>Pagos Flexibles</h4><p>Efectivo o transferencia bancaria.</p></div>
      <div class="beneficio"><span class="b-icono">&#128230;</span><h4>Seguimiento en Tiempo Real</h4><p>Rastrea tu pedido con tu c&oacute;digo en cualquier momento.</p></div>
    </div>
  </div>
</section>

<%@ include file="/jsp/footer.jsp" %>
<script src="<%= ctx %>/js/carrito.js"></script>
<script src="<%= ctx %>/js/filtros.js"></script>
</body>
</html>
