<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,modelo.Producto" %>
<%
    List<Producto> productos  = (List<Producto>) request.getAttribute("productos");
    String categoriaActual    = (String)  request.getAttribute("categoriaActual");
    String subActual          = (String)  request.getAttribute("subActual");
    String busqueda           = (String)  request.getAttribute("busqueda");
    Boolean soloDescuento     = (Boolean) request.getAttribute("soloDescuento");
    String ctx = request.getContextPath();
    if (categoriaActual == null) categoriaActual = "";
    if (subActual       == null) subActual       = "";
    if (busqueda        == null) busqueda        = "";
    if (soloDescuento   == null) soloDescuento   = false;

    String titulo = "Todos los Productos";
    if (!categoriaActual.isEmpty() && !subActual.isEmpty())
        titulo = categoriaActual.substring(0,1).toUpperCase()+categoriaActual.substring(1)
               + " " + subActual.substring(0,1).toUpperCase()+subActual.substring(1);
    else if (!categoriaActual.isEmpty())
        titulo = categoriaActual.substring(0,1).toUpperCase()+categoriaActual.substring(1);
    else if (soloDescuento) titulo = "Ofertas y Descuentos";
    else if (!busqueda.isEmpty()) titulo = "Resultados: " + busqueda;
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= titulo %> &mdash; MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>

<%@ include file="/jsp/navbar.jsp" %>

<div class="catalogo-layout">
  <aside class="sidebar-filtros">
    <h3>Filtros</h3>
    <form id="formFiltros" method="get" action="<%= ctx %>/catalogo">
      <div class="filtro-grupo">
        <h4>Categor&iacute;a</h4>
        <label class="filtro-radio"><input type="radio" name="categoria" value=""
          <%= categoriaActual.isEmpty() ? "checked" : "" %> onchange="this.form.submit()"> Todas</label>
        <label class="filtro-radio"><input type="radio" name="categoria" value="ropa"
          <%= "ropa".equals(categoriaActual) ? "checked" : "" %> onchange="this.form.submit()"> Ropa</label>
        <label class="filtro-radio"><input type="radio" name="categoria" value="zapatos"
          <%= "zapatos".equals(categoriaActual) ? "checked" : "" %> onchange="this.form.submit()"> Zapatos</label>
      </div>
      <div class="filtro-grupo">
        <h4>Para</h4>
        <label class="filtro-radio"><input type="radio" name="subcategoria" value=""
          <%= subActual.isEmpty() ? "checked" : "" %> onchange="this.form.submit()"> Todos</label>
        <label class="filtro-radio"><input type="radio" name="subcategoria" value="hombre"
          <%= "hombre".equals(subActual) ? "checked" : "" %> onchange="this.form.submit()"> Hombre</label>
        <label class="filtro-radio"><input type="radio" name="subcategoria" value="mujer"
          <%= "mujer".equals(subActual) ? "checked" : "" %> onchange="this.form.submit()"> Mujer</label>
      </div>
      <div class="filtro-grupo">
        <label class="filtro-check">
          <input type="checkbox" name="descuento" value="true"
                 <%= soloDescuento ? "checked" : "" %> onchange="this.form.submit()">
          Solo con descuento
        </label>
      </div>
      <% if (!categoriaActual.isEmpty() || !subActual.isEmpty() || soloDescuento || !busqueda.isEmpty()) { %>
        <a href="<%= ctx %>/catalogo" class="btn-limpiar-filtros">Limpiar filtros</a>
      <% } %>
    </form>
  </aside>

  <main class="catalogo-main">
    <div class="catalogo-header">
      <h1><%= titulo %></h1>
      <span class="catalogo-count"><%= productos != null ? productos.size() : 0 %> producto(s)</span>
    </div>

    <% if (productos == null || productos.isEmpty()) { %>
      <div class="estado-vacio">
        <span>&#128269;</span>
        <h3>No encontramos productos</h3>
        <p>Prueba con otros filtros o b&uacute;squeda diferente.</p>
        <a href="<%= ctx %>/catalogo" class="btn-primario">Ver todos los productos</a>
      </div>
    <% } else { %>
      <div class="productos-grid">
        <% for (Producto p : productos) {
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
              <% if (p.isStockBajo() && !"agotado".equals(estado)) { %>
                <p class="stock-bajo">Quedan <%= p.getStockTotal() %> unidades</p>
              <% } %>
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
          <% if (!"agotado".equals(estado) && !"inactivo".equals(estado)) { %>
            <a href="<%= ctx %>/producto?id=<%= p.getId() %>" class="btn-agregar-rapido">Ver producto</a>
          <% } else { %>
            <button class="btn-agregar-rapido btn-deshabilitado" disabled>Agotado</button>
          <% } %>
        </div>
        <% } %>
      </div>
    <% } %>
  </main>
</div>

<%@ include file="/jsp/footer.jsp" %>
<script src="<%= ctx %>/js/filtros.js"></script>
</body>
</html>
