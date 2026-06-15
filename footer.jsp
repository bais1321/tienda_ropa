<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String footerCtx = request.getContextPath();
%>
<footer class="footer">
  <div class="footer-inner">
    <div class="footer-col">
      <h4>MiTienda</h4>
      <p>Ropa y zapatos con estilo para hombre y mujer.</p>
    </div>
    <div class="footer-col">
      <h4>Cat&aacute;logo</h4>
      <ul>
        <li><a href="<%= footerCtx %>/catalogo?categoria=ropa&subcategoria=hombre">Ropa Hombre</a></li>
        <li><a href="<%= footerCtx %>/catalogo?categoria=ropa&subcategoria=mujer">Ropa Mujer</a></li>
        <li><a href="<%= footerCtx %>/catalogo?categoria=zapatos&subcategoria=hombre">Zapatos Hombre</a></li>
        <li><a href="<%= footerCtx %>/catalogo?categoria=zapatos&subcategoria=mujer">Zapatos Mujer</a></li>
        <li><a href="<%= footerCtx %>/catalogo?descuento=true">Ofertas</a></li>
      </ul>
    </div>
    <div class="footer-col">
      <h4>Mi Cuenta</h4>
      <ul>
        <li><a href="<%= footerCtx %>/login">Iniciar Sesi&oacute;n</a></li>
        <li><a href="<%= footerCtx %>/registro">Crear Cuenta</a></li>
        <li><a href="<%= footerCtx %>/historial">Mis Pedidos</a></li>
        <li><a href="<%= footerCtx %>/rastreo">Rastrear Pedido</a></li>
      </ul>
    </div>
    <div class="footer-col">
      <h4>Contacto</h4>
      <p>&#128241; WhatsApp disponible para atenci&oacute;n al cliente</p>
      <p>&#128230; Entrega personal y a domicilio</p>
    </div>
  </div>
  <div class="footer-bottom">
    <p>&copy; 2026 MiTienda. Todos los derechos reservados.</p>
  </div>
</footer>
