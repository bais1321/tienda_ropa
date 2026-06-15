<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ page import="modelo.ItemCarrito" %>
<%@ page import="modelo.Usuario" %>
<%
    /*
     * SOLUCIÓN DEFINITIVA:
     * ConfigTienda NO se castea aquí.
     * CheckoutServlet ya extrajo costoEnvio, minimoDomicilio y datosBancarios
     * como atributos simples (BigDecimal y String) antes de llegar al JSP.
     */
    List<ItemCarrito> carrito  = (List<ItemCarrito>) request.getAttribute("carrito");
    BigDecimal subtotal        = (BigDecimal) request.getAttribute("subtotal");
    BigDecimal descuento5      = (BigDecimal) request.getAttribute("descuento5");
    Boolean    puedeDesc5      = (Boolean)    request.getAttribute("puedeDesc5");
    BigDecimal costoEnvio      = (BigDecimal) request.getAttribute("costoEnvio");
    BigDecimal minimoDomicilio = (BigDecimal) request.getAttribute("minimoDomicilio");
    String     datosBancarios  = (String)     request.getAttribute("datosBancarios");
    Usuario    usuario         = (Usuario)    request.getAttribute("usuario");
    String ctx = request.getContextPath();

    if (puedeDesc5      == null) puedeDesc5      = false;
    if (subtotal        == null) subtotal        = BigDecimal.ZERO;
    if (descuento5      == null) descuento5      = BigDecimal.ZERO;
    if (costoEnvio      == null) costoEnvio      = BigDecimal.ZERO;
    if (minimoDomicilio == null) minimoDomicilio = new BigDecimal("100.00");
    if (datosBancarios  == null) datosBancarios  = "Configura los datos bancarios en el panel admin.";

    String valNombre   = usuario != null ? usuario.getNombre()   : "";
    String valApellido = usuario != null ? usuario.getApellido() : "";
    String valTelefono = usuario != null && usuario.getTelefono() != null
                         ? usuario.getTelefono() : "";
    String valEmail    = usuario != null ? usuario.getEmail() : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Pagar — MiTienda</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>

<%@ include file="/jsp/navbar.jsp" %>

<div class="contenedor pagina-checkout">

  <div class="stepper">
    <div class="step step-activo" id="step-ind-1">
      <div class="step-num">1</div><span>Datos</span>
    </div>
    <div class="step-linea"></div>
    <div class="step" id="step-ind-2">
      <div class="step-num">2</div><span>Entrega</span>
    </div>
    <div class="step-linea"></div>
    <div class="step" id="step-ind-3">
      <div class="step-num">3</div><span>Pago</span>
    </div>
  </div>

  <div class="checkout-layout">
    <form action="<%= ctx %>/pedido" method="post" id="formCheckout">

      <!-- PASO 1 -->
      <div class="paso" id="paso1">
        <h2>① Datos Personales</h2>
        <div class="form-grid-2">
          <div class="form-grupo">
            <label>Nombre *</label>
            <input type="text" name="nombre" required value="<%= valNombre %>" placeholder="Tu nombre">
          </div>
          <div class="form-grupo">
            <label>Apellido *</label>
            <input type="text" name="apellido" required value="<%= valApellido %>" placeholder="Tu apellido">
          </div>
          <div class="form-grupo">
            <label>Teléfono / WhatsApp</label>
            <input type="tel" name="telefono" value="<%= valTelefono %>" placeholder="502XXXXXXXX">
          </div>
          <div class="form-grupo">
            <label>Correo Electrónico *</label>
            <input type="email" name="email" required value="<%= valEmail %>" placeholder="tu@correo.com">
          </div>
        </div>
        <button type="button" class="btn-primario btn-grande" onclick="irPaso(2)">Siguiente →</button>
      </div>

      <!-- PASO 2 -->
      <div class="paso oculto" id="paso2">
        <h2>② Método de Entrega</h2>
        <div class="opciones-entrega">
          <label class="opcion-card opcion-activa" id="cardPersonal">
            <input type="radio" name="tipoEntrega" value="personal" checked
                   onchange="cambiarEntrega('personal')">
            <div class="opcion-contenido">
              <span class="opcion-icono">📍</span>
              <div>
                <h4>Entrega Personal</h4>
                <p>Coordina un punto de encuentro con nosotros.</p>
                <span class="badge-gratis">Sin costo adicional</span>
              </div>
            </div>
          </label>
          <label class="opcion-card" id="cardDomicilio">
            <input type="radio" name="tipoEntrega" value="domicilio"
                   onchange="cambiarEntrega('domicilio')">
            <div class="opcion-contenido">
              <span class="opcion-icono">🚚</span>
              <div>
                <h4>Envío a Domicilio</h4>
                <p>Recíbelo en la puerta de tu casa.</p>
                <span class="badge-costo">Q<%= costoEnvio %> (mín. Q<%= minimoDomicilio %>)</span>
              </div>
            </div>
          </label>
        </div>
        <div class="form-grupo oculto" id="campoDireccion">
          <label>Dirección de entrega *</label>
          <textarea name="direccion" rows="3" placeholder="Zona, colonia, calle, número..."></textarea>
        </div>
        <div class="paso-botones">
          <button type="button" class="btn-secundario" onclick="irPaso(1)">← Atrás</button>
          <button type="button" class="btn-primario btn-grande" onclick="irPaso(3)">Siguiente →</button>
        </div>
      </div>

      <!-- PASO 3 -->
      <div class="paso oculto" id="paso3">
        <h2>③ Método de Pago</h2>
        <div class="opciones-pago">
          <label class="opcion-card opcion-activa" id="cardEfectivo">
            <input type="radio" name="tipoPago" value="efectivo" checked
                   onchange="cambiarPago('efectivo')">
            <div class="opcion-contenido">
              <span class="opcion-icono">💵</span>
              <div><h4>Efectivo</h4><p>Paga en el momento de la entrega.</p></div>
            </div>
          </label>
          <label class="opcion-card" id="cardTransferencia">
            <input type="radio" name="tipoPago" value="transferencia"
                   onchange="cambiarPago('transferencia')">
            <div class="opcion-contenido">
              <span class="opcion-icono">🏦</span>
              <div><h4>Transferencia Bancaria</h4><p>Envía el comprobante por WhatsApp.</p></div>
            </div>
          </label>
        </div>
        <div class="datos-bancarios oculto" id="datosBancarios">
          <h4>📋 Datos para transferencia:</h4>
          <pre><%= datosBancarios %></pre>
          <p class="nota-whatsapp">📱 Envía el comprobante por WhatsApp después de la transferencia.</p>
        </div>
        <div class="paso-botones">
          <button type="button" class="btn-secundario" onclick="irPaso(2)">← Atrás</button>
          <button type="button" class="btn-primario btn-grande" onclick="mostrarResumen()">Ver resumen →</button>
        </div>
      </div>

      <!-- PASO 4 -->
      <div class="paso oculto" id="paso4">
        <h2>④ Confirmar Pedido</h2>
        <div class="resumen-checkout">
          <h4>Productos:</h4>
          <% for (ItemCarrito item : carrito) { %>
          <div class="resumen-item">
            <img src="<%= ctx %><%= item.getImagenPrincipal() %>" alt="">
            <div>
              <p><strong><%= item.getNombreProducto() %></strong></p>
              <p>Talla: <%= item.getTalla() %> · Cant: <%= item.getCantidad() %></p>
            </div>
            <span>Q<%= item.getSubtotalLinea() %></span>
          </div>
          <% } %>
          <div class="resumen-totales">
            <div class="resumen-linea"><span>Subtotal</span><span>Q<%= subtotal %></span></div>
            <% if (puedeDesc5 && descuento5.compareTo(BigDecimal.ZERO) > 0) { %>
            <div class="resumen-linea resumen-descuento">
              <span>🎉 Descuento primera compra (5%)</span>
              <span>-Q<%= descuento5 %></span>
            </div>
            <% } %>
            <div class="resumen-linea">
              <span>Envío</span>
              <span id="textoEnvioResumen">Q0.00 (entrega personal)</span>
            </div>
            <div class="resumen-total">
              <span>TOTAL</span>
              <span class="precio-final" id="textoTotalResumen">
                Q<%= subtotal.subtract(descuento5).setScale(2, RoundingMode.HALF_UP) %>
              </span>
            </div>
          </div>
        </div>
        <div class="paso-botones">
          <button type="button" class="btn-secundario" onclick="irPaso(3)">← Atrás</button>
          <button type="submit" class="btn-primario btn-grande btn-confirmar">✅ Confirmar Pedido</button>
        </div>
      </div>

    </form>
  </div>
</div>

<%@ include file="/jsp/footer.jsp" %>
<script>
var costoEnvioVal = <%= costoEnvio %>;
var subtotalVal   = <%= subtotal %>;
var descuento5Val = <%= descuento5 %>;
var esADomicilio  = false;

function irPaso(num) {
  document.querySelectorAll('.paso').forEach(function(p) { p.classList.add('oculto'); });
  document.getElementById('paso' + num).classList.remove('oculto');
  document.querySelectorAll('.step').forEach(function(s, i) {
    s.classList.remove('step-activo', 'step-completado');
    if (i + 1 < num)  s.classList.add('step-completado');
    if (i + 1 === num) s.classList.add('step-activo');
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
function cambiarEntrega(tipo) {
  esADomicilio = (tipo === 'domicilio');
  var campo = document.getElementById('campoDireccion');
  campo.classList.toggle('oculto', !esADomicilio);
  campo.querySelector('textarea').required = esADomicilio;
  if (!esADomicilio) campo.querySelector('textarea').value = '';
  actualizarTotal();
  document.querySelectorAll('.opciones-entrega .opcion-card').forEach(function(c){ c.classList.remove('opcion-activa'); });
  document.getElementById(esADomicilio ? 'cardDomicilio' : 'cardPersonal').classList.add('opcion-activa');
}
function cambiarPago(tipo) {
  document.getElementById('datosBancarios').classList.toggle('oculto', tipo !== 'transferencia');
  document.querySelectorAll('.opciones-pago .opcion-card').forEach(function(c){ c.classList.remove('opcion-activa'); });
  document.getElementById(tipo === 'transferencia' ? 'cardTransferencia' : 'cardEfectivo').classList.add('opcion-activa');
}
function actualizarTotal() {
  var envio = esADomicilio ? costoEnvioVal : 0;
  var total = (subtotalVal - descuento5Val + envio).toFixed(2);
  document.getElementById('textoEnvioResumen').textContent = envio > 0 ? 'Q' + envio.toFixed(2) : 'Q0.00 (entrega personal)';
  document.getElementById('textoTotalResumen').textContent = 'Q' + total;
}
function mostrarResumen() { actualizarTotal(); irPaso(4); }
</script>
</body>
</html>
