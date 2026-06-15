<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="modelo.Usuario" %>
<%@ page import="servlet.CarritoServlet" %>
<%
    String navCtx       = request.getContextPath();
    Usuario usuarioNav  = (Usuario) session.getAttribute("usuario");
    int cantidadCarrito = CarritoServlet.contarItems(request);
    String uriActual    = request.getRequestURI();

    String toastMsg  = (String) session.getAttribute("toast");
    String toastTipo = (String) session.getAttribute("toastTipo");
    if (toastMsg  != null) { session.removeAttribute("toast"); session.removeAttribute("toastTipo"); }
    if (toastTipo == null) toastTipo = "exito";

    String claseInicio   = uriActual.endsWith("/") || uriActual.endsWith("home") ? "activo" : "";
    String claseCatalogo = uriActual.contains("catalogo") ? "activo" : "";
    String claseRastreo  = uriActual.contains("rastreo")  ? "activo" : "";
%>
<nav class="navbar">
  <div class="nav-inner">
    <a href="<%= navCtx %>/" class="nav-logo">
      <span class="logo-icono">&#128247;</span>
      <span class="logo-texto">MiTienda</span>
    </a>
    <ul class="nav-links">
      <li><a href="<%= navCtx %>/" class="<%= claseInicio %>">Inicio</a></li>
      <li class="dropdown">
        <a href="<%= navCtx %>/catalogo" class="<%= claseCatalogo %>">
          Cat&aacute;logo <span class="flecha">&#9662;</span>
        </a>
        <ul class="dropdown-menu">
          <li><a href="<%= navCtx %>/catalogo?categoria=ropa&subcategoria=hombre">Ropa Hombre</a></li>
          <li><a href="<%= navCtx %>/catalogo?categoria=ropa&subcategoria=mujer">Ropa Mujer</a></li>
          <li><a href="<%= navCtx %>/catalogo?categoria=zapatos&subcategoria=hombre">Zapatos Hombre</a></li>
          <li><a href="<%= navCtx %>/catalogo?categoria=zapatos&subcategoria=mujer">Zapatos Mujer</a></li>
          <li class="sep"></li>
          <li><a href="<%= navCtx %>/catalogo?descuento=true">&#127991; Ofertas</a></li>
        </ul>
      </li>
      <li><a href="<%= navCtx %>/rastreo" class="<%= claseRastreo %>">Rastrear Pedido</a></li>
    </ul>
    <div class="nav-busqueda">
      <input type="text" id="inputBusqueda" placeholder="Buscar productos..."
             autocomplete="off" oninput="buscarEnTiempoReal(this.value)">
      <div id="resultadosBusqueda" class="resultados-busqueda oculto"></div>
    </div>
    <div class="nav-acciones">
      <a href="<%= navCtx %>/carrito" class="btn-carrito" id="btnCarrito">
        <span>&#128722;</span>
        <% if (cantidadCarrito > 0) { %>
          <span class="badge-carrito" id="badgeCarrito"><%= cantidadCarrito %></span>
        <% } else { %>
          <span class="badge-carrito oculto" id="badgeCarrito">0</span>
        <% } %>
      </a>
      <% if (usuarioNav != null && !usuarioNav.isEsAdmin()) { %>
        <div class="dropdown">
          <button class="btn-usuario">&#128100; <%= usuarioNav.getNombre() %> &#9662;</button>
          <ul class="dropdown-menu dm-derecha">
            <li><a href="<%= navCtx %>/historial">Mis Pedidos</a></li>
            <li class="sep"></li>
            <li><a href="<%= navCtx %>/logout">Cerrar Sesi&oacute;n</a></li>
          </ul>
        </div>
      <% } else if (usuarioNav != null && usuarioNav.isEsAdmin()) { %>
        <a href="<%= navCtx %>/admin/dashboard" class="btn-admin">Panel Admin</a>
      <% } else { %>
        <a href="<%= navCtx %>/login"    class="btn-nav-outline">Iniciar Sesi&oacute;n</a>
        <a href="<%= navCtx %>/registro" class="btn-nav-filled">Registrarse</a>
      <% } %>
    </div>
    <button class="hamburguesa" onclick="toggleMenu()">&#9776;</button>
  </div>
</nav>
<% if (toastMsg != null) { %>
<div class="toast toast-<%= toastTipo %>" id="toastGlobal">
  <%= "exito".equals(toastTipo) ? "OK" : "Error" %>: <%= toastMsg %>
</div>
<script>
  setTimeout(function() {
    var t = document.getElementById('toastGlobal');
    if (t) { t.classList.add('toast-salir'); setTimeout(function(){ t.remove(); }, 400); }
  }, 3500);
</script>
<% } %>
