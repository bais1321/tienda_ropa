<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="modelo.ConfigTienda" %>
<%
    String ctx = request.getContextPath();
    modelo.ConfigTienda c = (modelo.ConfigTienda) request.getAttribute("config");
    String error = (String) request.getAttribute("error");
    if (c == null) c = new modelo.ConfigTienda();

    String valNombreTienda = c.getNombreTienda()          != null ? c.getNombreTienda()          : "";
    String valWhatsapp     = c.getNumeroWhatsapp()        != null ? c.getNumeroWhatsapp()        : "";
    String valDatosBanc    = c.getDatosBancarios()        != null ? c.getDatosBancarios()        : "";
    String valEmailNotif   = c.getEmailNotificaciones()   != null ? c.getEmailNotificaciones()   : "";
    String valSmtpHost     = c.getSmtpHost()              != null ? c.getSmtpHost()              : "smtp.gmail.com";
    int    valSmtpPuerto   = c.getSmtpPuerto() > 0 ? c.getSmtpPuerto() : 587;
    String valCostoEnvio   = c.getCostoEnvioDomicilio()   != null ? c.getCostoEnvioDomicilio().toPlainString()   : "20.00";
    String valMinimoDom    = c.getMinimoDomicilio()       != null ? c.getMinimoDomicilio().toPlainString()       : "100.00";
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Configuración — Admin MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
  <link rel="stylesheet" href="<%= ctx %>/css/admin.css">
</head>
<body class="admin-body">
<%@ include file="/jsp/admin/navbar_admin.jsp" %>
<div class="admin-contenedor">
  <h1 class="admin-titulo">&#9881; Configuración de la Tienda</h1>
  <% if (error != null) { %><div class="alerta alerta-error"><%= error %></div><% } %>
  <form action="<%= ctx %>/admin/config" method="post" enctype="multipart/form-data">
    <div class="admin-dos-col">
      <div class="admin-card">
        <h3>&#127978; Datos Generales</h3>
        <div class="form-grupo">
          <label>Nombre de la Tienda *</label>
          <input type="text" name="nombreTienda" required value="<%= valNombreTienda %>" placeholder="Ej: Mi Tienda Fashion">
        </div>
        <div class="form-grupo">
          <label>Logo de la Tienda</label>
          <% if (c.getUrlLogo() != null && !c.getUrlLogo().isEmpty()) { %>
            <div class="logo-actual">
              <img src="<%= ctx %><%= c.getUrlLogo() %>" alt="Logo actual" class="preview-logo">
              <p class="form-hint">Logo actual. Sube uno nuevo para reemplazarlo.</p>
            </div>
          <% } %>
          <input type="file" name="logo" accept="image/*">
        </div>
        <div class="form-grupo">
          <label>Número de WhatsApp *</label>
          <input type="text" name="numeroWhatsapp" required value="<%= valWhatsapp %>" placeholder="502XXXXXXXXX">
          <small class="form-hint">Con código de país, sin + ni espacios. Ej: 50299999999</small>
        </div>
        <div class="form-grupo">
          <label>Datos Bancarios</label>
          <textarea name="datosBancarios" rows="5" placeholder="Banco: ...&#10;Cuenta: ..."><%= valDatosBanc %></textarea>
          <small class="form-hint">Se muestra a clientes que pagan por transferencia.</small>
        </div>
      </div>
      <div>
        <div class="admin-card">
          <h3>&#128666; Envío y Precios</h3>
          <div class="form-grupo">
            <label>Costo de Envío a Domicilio (Q) *</label>
            <input type="number" name="costoEnvio" step="0.01" min="0" required value="<%= valCostoEnvio %>">
          </div>
          <div class="form-grupo">
            <label>Compra Mínima para Domicilio (Q) *</label>
            <input type="number" name="minimoDomicilio" step="0.01" min="0" required value="<%= valMinimoDom %>">
          </div>
        </div>
        <div class="admin-card">
          <h3>&#128231; Configuración de Correo (SMTP)</h3>
          <div class="alerta alerta-info">
            Para Gmail usa una <strong>App Password</strong> de Google.<br>
            <em>Google Account → Security → App Passwords</em>
          </div>
          <div class="form-grupo">
            <label>Correo de Notificaciones</label>
            <input type="email" name="emailNotificaciones" value="<%= valEmailNotif %>" placeholder="notificaciones@tutienda.com">
          </div>
          <div class="form-grid-2">
            <div class="form-grupo">
              <label>Servidor SMTP</label>
              <input type="text" name="smtpHost" value="<%= valSmtpHost %>">
            </div>
            <div class="form-grupo">
              <label>Puerto SMTP</label>
              <input type="number" name="smtpPuerto" value="<%= valSmtpPuerto %>">
            </div>
          </div>
          <div class="form-grupo">
            <label>App Password de Gmail</label>
            <input type="password" name="smtpPassword" placeholder="Deja vacío para no cambiarla">
            <small class="form-hint">Solo ingresa si quieres cambiarla.</small>
          </div>
        </div>
      </div>
    </div>
    <div style="text-align:right;margin-top:16px">
      <button type="submit" class="btn-primario btn-grande">&#128190; Guardar Configuración</button>
    </div>
  </form>
</div>
</body>
</html>
