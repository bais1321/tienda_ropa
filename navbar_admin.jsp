<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="modelo.Usuario" %>
<%
    String adminCtx  = request.getContextPath();
    Usuario adminUser = (Usuario) session.getAttribute("usuario");
    String uriAdmin   = request.getRequestURI();

    String toastMsg  = (String) session.getAttribute("toast");
    String toastTipo = (String) session.getAttribute("toastTipo");
    if (toastMsg  != null) { session.removeAttribute("toast"); session.removeAttribute("toastTipo"); }
    if (toastTipo == null) toastTipo = "exito";

    String claseDash     = uriAdmin.contains("dashboard") ? "activo" : "";
    String claseProds    = uriAdmin.contains("productos")  ? "activo" : "";
    String clasePeds     = uriAdmin.contains("pedidos")    ? "activo" : "";
    String claseConf     = uriAdmin.contains("config")     ? "activo" : "";
%>
<nav class="admin-navbar">
  <div class="admin-nav-inner">
    <a href="<%= adminCtx %>/admin/dashboard" class="admin-logo">
      &#9881; Panel Admin
    </a>
    <ul class="admin-nav-links">
      <li><a href="<%= adminCtx %>/admin/dashboard" class="<%= claseDash %>">&#128202; Dashboard</a></li>
      <li><a href="<%= adminCtx %>/admin/productos"  class="<%= claseProds %>">&#128247; Productos</a></li>
      <li><a href="<%= adminCtx %>/admin/pedidos"    class="<%= clasePeds %>">&#128230; Pedidos</a></li>
      <li><a href="<%= adminCtx %>/admin/config"     class="<%= claseConf %>">&#9881; Configuraci&oacute;n</a></li>
    </ul>
    <div class="admin-user-info">
      <span>&#128100; <%= adminUser != null ? adminUser.getNombre() : "Admin" %></span>
      <a href="<%= adminCtx %>/logout" class="btn-logout">Salir</a>
    </div>
  </div>
</nav>
<% if (toastMsg != null) { %>
<div class="toast toast-<%= toastTipo %>" id="toastAdmin">
  <%= "exito".equals(toastTipo) ? "OK" : "Error" %>: <%= toastMsg %>
</div>
<script>
  setTimeout(function() {
    var t = document.getElementById('toastAdmin');
    if (t) { t.classList.add('toast-salir'); setTimeout(function(){ t.remove(); }, 400); }
  }, 3500);
</script>
<% } %>
