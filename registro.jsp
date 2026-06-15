<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String ctx      = request.getContextPath();
    String error    = (String) request.getAttribute("error");
    String fNombre  = (String) request.getAttribute("fNombre");
    String fApellido= (String) request.getAttribute("fApellido");
    String fTelefono= (String) request.getAttribute("fTelefono");
    String fEmail   = (String) request.getAttribute("fEmail");
    String pNombre  = request.getParameter("nombre");
    String pApellido= request.getParameter("apellido");
    String pEmail   = request.getParameter("email");
    String pTelefono= request.getParameter("telefono");
    if (fNombre   == null) fNombre   = pNombre   != null ? pNombre   : "";
    if (fApellido == null) fApellido = pApellido != null ? pApellido : "";
    if (fEmail    == null) fEmail    = pEmail    != null ? pEmail    : "";
    if (fTelefono == null) fTelefono = pTelefono != null ? pTelefono : "";
    boolean esPostCompra = pEmail != null && !pEmail.isEmpty();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Crear Cuenta — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body class="bg-gris">
<%@ include file="/jsp/navbar.jsp" %>
<div class="auth-wrapper">
  <div class="auth-card auth-card-lg">
    <div class="auth-header">
      <% if (esPostCompra) { %>
        <h2>&#127881; ¡Crea tu cuenta!</h2>
        <p>Tus datos ya están pre-llenados. Solo agrega tu contraseña.</p>
      <% } else { %>
        <h2>Crear Cuenta</h2>
        <p>Regístrate y obtén el 5% de descuento en tu primera compra.</p>
      <% } %>
    </div>
    <% if (!esPostCompra) { %>
    <div class="beneficio-registro">
      &#127881; <strong>Crea tu cuenta hoy</strong> y tienes <strong>10 días</strong> para usar tu descuento del 5%.
    </div>
    <% } %>
    <% if (error != null) { %><div class="alerta alerta-error"><%= error %></div><% } %>
    <form action="<%= ctx %><%= esPostCompra ? "/crear-cuenta" : "/registro" %>" method="post">
      <div class="form-grid-2">
        <div class="form-grupo"><label>Nombre *</label><input type="text" name="nombre" required value="<%= fNombre %>" placeholder="Tu nombre"></div>
        <div class="form-grupo"><label>Apellido *</label><input type="text" name="apellido" required value="<%= fApellido %>" placeholder="Tu apellido"></div>
        <div class="form-grupo"><label>Teléfono / WhatsApp</label><input type="tel" name="telefono" value="<%= fTelefono %>" placeholder="502XXXXXXXX"></div>
        <div class="form-grupo"><label>Correo Electrónico *</label><input type="email" name="email" required value="<%= fEmail %>" placeholder="tu@correo.com"></div>
        <div class="form-grupo"><label>Contraseña *</label><input type="password" name="password" required minlength="6" placeholder="Mínimo 6 caracteres"></div>
        <div class="form-grupo"><label>Confirmar Contraseña *</label><input type="password" name="confirmar" required minlength="6" placeholder="Repite tu contraseña"></div>
      </div>
      <button type="submit" class="btn-primario btn-grande btn-bloque">
        <%= esPostCompra ? "Crear mi cuenta →" : "Crear Cuenta Gratis →" %>
      </button>
    </form>
    <% if (!esPostCompra) { %>
    <div class="auth-footer">
      <p>¿Ya tienes cuenta? <a href="<%= ctx %>/login">Iniciar Sesión →</a></p>
    </div>
    <% } %>
  </div>
</div>
<%@ include file="/jsp/footer.jsp" %>
</body>
</html>
