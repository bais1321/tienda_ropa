<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String ctx           = request.getContextPath();
    String error         = (String) request.getAttribute("error");
    String emailIngresado= (String) request.getAttribute("emailIngresado");
    if (emailIngresado == null) emailIngresado = "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Iniciar Sesión — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body class="bg-gris">
<%@ include file="/jsp/navbar.jsp" %>
<div class="auth-wrapper">
  <div class="auth-card">
    <div class="auth-header">
      <h2>Bienvenido de vuelta</h2>
      <p>Inicia sesión en tu cuenta</p>
    </div>
    <% if (error != null) { %><div class="alerta alerta-error"><%= error %></div><% } %>
    <form action="<%= ctx %>/login" method="post">
      <div class="form-grupo">
        <label>Correo Electrónico</label>
        <input type="email" name="email" required autofocus value="<%= emailIngresado %>" placeholder="tu@correo.com">
      </div>
      <div class="form-grupo">
        <label>Contraseña</label>
        <div class="input-password">
          <input type="password" name="password" id="inputPass" required placeholder="Tu contraseña" minlength="6">
          <button type="button" class="btn-toggle-pass" onclick="togglePass()">&#128065;</button>
        </div>
      </div>
      <button type="submit" class="btn-primario btn-grande btn-bloque">Iniciar Sesión</button>
    </form>
    <div class="auth-footer">
      <p>¿No tienes cuenta? <a href="<%= ctx %>/registro">Crear cuenta gratis →</a></p>
    </div>
  </div>
</div>
<%@ include file="/jsp/footer.jsp" %>
<script>
function togglePass() {
  var i = document.getElementById('inputPass');
  i.type = (i.type === 'password') ? 'text' : 'password';
}
</script>
</body>
</html>
