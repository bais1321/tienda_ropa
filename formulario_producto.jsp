<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="modelo.Producto" %>
<%@ page import="modelo.TallaProducto" %>
<%@ page import="modelo.ImagenProducto" %>
<%@ page import="java.util.List" %>
<%
    Producto p   = (Producto) request.getAttribute("producto");
    String error = (String)   request.getAttribute("error");
    boolean esEdicion = p != null;
    String ctx = request.getContextPath();

    // CORRECCIÓN líneas 58 y 64: valores extraídos a variables Java
    // para no anidar comillas dobles dentro de atributos HTML
    String valPrecio    = esEdicion ? p.getPrecioOriginal().toPlainString() : "";
    String valDescuento = esEdicion ? String.valueOf(p.getDescuento()) : "0";
    String valNombre    = esEdicion ? p.getNombre() : "";
    String valDesc      = esEdicion && p.getDescripcion() != null ? p.getDescripcion() : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= esEdicion ? "Editar" : "Nuevo" %> Producto — Admin</title>
  <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
  <link rel="stylesheet" href="<%= ctx %>/css/admin.css">
</head>
<body class="admin-body">

<%@ include file="/jsp/admin/navbar_admin.jsp" %>

<div class="admin-contenedor">
  <div class="admin-page-header">
    <h1 class="admin-titulo"><%= esEdicion ? "✏ Editar Producto" : "➕ Nuevo Producto" %></h1>
    <a href="<%= ctx %>/admin/productos" class="btn-secundario">← Volver</a>
  </div>

  <% if (error != null) { %>
    <div class="alerta alerta-error"><%= error %></div>
  <% } %>

  <div class="admin-dos-col-asimetrico">

    <!-- ═══ FORMULARIO PRINCIPAL ══════════════════════════ -->
    <div class="admin-card">
      <h3>Datos del Producto</h3>
      <form action="<%= ctx %>/admin/productos" method="post" enctype="multipart/form-data">
        <input type="hidden" name="accion" value="<%= esEdicion ? "actualizar" : "crear" %>">
        <% if (esEdicion) { %>
          <input type="hidden" name="id" value="<%= p.getId() %>">
        <% } %>

        <div class="form-grupo">
          <label>Nombre del Producto *</label>
          <input type="text" name="nombre" required
                 value="<%= valNombre %>"
                 placeholder="Ej: Camiseta Oversize Negra">
        </div>

        <div class="form-grupo">
          <label>Descripción</label>
          <textarea name="descripcion" rows="4"
                    placeholder="Descripción del producto..."><%= valDesc %></textarea>
        </div>

        <div class="form-grid-2">

          <%-- CORRECCIÓN línea 58: valor en variable Java 'valPrecio' --%>
          <div class="form-grupo">
            <label>Precio Original (Q) *</label>
            <input type="number" name="precioOriginal" step="0.01" min="0.01" required
                   value="<%= valPrecio %>"
                   placeholder="0.00">
          </div>

          <%-- CORRECCIÓN línea 64: valor en variable Java 'valDescuento' --%>
          <div class="form-grupo">
            <label>Descuento (%)</label>
            <input type="number" name="descuento" min="0" max="99"
                   value="<%= valDescuento %>"
                   placeholder="0 = sin descuento">
          </div>

          <div class="form-grupo">
            <label>Categoría *</label>
            <select name="categoria" required>
              <option value="ropa"    <%= esEdicion && "ropa".equals(p.getCategoria())    ? "selected" : "" %>>Ropa</option>
              <option value="zapatos" <%= esEdicion && "zapatos".equals(p.getCategoria()) ? "selected" : "" %>>Zapatos</option>
            </select>
          </div>

          <div class="form-grupo">
            <label>Subcategoría *</label>
            <select name="subcategoria" required>
              <option value="hombre" <%= esEdicion && "hombre".equals(p.getSubcategoria()) ? "selected" : "" %>>Hombre</option>
              <option value="mujer"  <%= esEdicion && "mujer".equals(p.getSubcategoria())  ? "selected" : "" %>>Mujer</option>
            </select>
          </div>
        </div>

        <div class="form-grupo">
          <label>Estado</label>
          <select name="estado">
            <option value="activo"   <%= !esEdicion || "activo".equals(p.getEstado())   ? "selected" : "" %>>Activo</option>
            <option value="inactivo" <%= esEdicion  && "inactivo".equals(p.getEstado()) ? "selected" : "" %>>Inactivo (oculto)</option>
          </select>
        </div>

        <div class="form-grupo form-check">
          <label class="check-label">
            <input type="checkbox" name="esDestacado"
                   <%= esEdicion && p.isEsDestacado() ? "checked" : "" %>>
            ⭐ Mostrar en sección destacados del home
          </label>
        </div>

        <!-- Imágenes nuevas -->
        <div class="form-grupo">
          <label><%= esEdicion ? "Agregar más imágenes" : "Imágenes del producto" %> (JPG, PNG, WEBP · Máx 5MB c/u)</label>
          <input type="file" name="imagenes" multiple accept="image/*"
                 onchange="previsualizarImagenes(this)">
          <div id="previewImagenes" class="preview-imagenes"></div>
        </div>

        <!-- Tallas solo para producto nuevo -->
        <% if (!esEdicion) { %>
        <div class="form-grupo">
          <label>Tallas iniciales</label>
          <div id="tallasNuevas">
            <div class="talla-nueva-fila">
              <input type="text"   name="tallaNombre" placeholder="Ej: S, M, 35" style="width:80px">
              <input type="number" name="tallaStock"  placeholder="Stock" min="0" style="width:80px">
              <button type="button" onclick="agregarFilaTalla()" class="btn-sm btn-secundario">+ Talla</button>
            </div>
          </div>
        </div>
        <% } %>

        <button type="submit" class="btn-primario btn-grande btn-bloque">
          <%= esEdicion ? "💾 Guardar Cambios" : "✅ Crear Producto" %>
        </button>
      </form>
    </div>

    <!-- ═══ PANEL LATERAL: TALLAS E IMÁGENES ═════════════ -->
    <% if (esEdicion) { %>
    <div>

      <!-- Imágenes actuales -->
      <div class="admin-card">
        <h3>🖼 Imágenes Actuales (<%= p.getImagenes().size() %>)</h3>
        <div class="imagenes-actuales">
          <% for (ImagenProducto img : p.getImagenes()) { %>
          <div class="img-actual-item">
            <img src="<%= ctx %><%= img.getRutaImagen() %>" alt="">
            <% if (img.isPrincipal()) { %>
              <span class="badge-principal">Principal</span>
            <% } %>
            <form action="<%= ctx %>/admin/productos" method="get" style="margin:0">
              <input type="hidden" name="accion"     value="eliminarImagen">
              <input type="hidden" name="imgId"      value="<%= img.getId() %>">
              <input type="hidden" name="productoId" value="<%= p.getId() %>">
              <button type="submit" class="btn-eliminar-img"
                      onclick="return confirm('¿Eliminar esta imagen?')">✕</button>
            </form>
          </div>
          <% } %>
        </div>
      </div>

      <!-- Gestión de tallas -->
      <div class="admin-card">
        <h3>📏 Tallas y Stock</h3>
        <table class="admin-tabla">
          <thead><tr><th>Talla</th><th>Stock</th><th>Eliminar</th></tr></thead>
          <tbody>
            <% for (TallaProducto t : p.getTallas()) { %>
            <tr>
              <td><strong><%= t.getTalla() %></strong></td>
              <td>
                <%-- CORRECCIÓN línea 170: se eliminó el style inline con comillas
                     problemáticas y se reemplazó por una clase CSS --%>
                <form action="<%= ctx %>/admin/productos" method="post" class="form-agregar-talla">
                  <input type="hidden" name="accion"     value="actualizarStockTalla">
                  <input type="hidden" name="tallaId"    value="<%= t.getId() %>">
                  <input type="hidden" name="productoId" value="<%= p.getId() %>">
                  <input type="number" name="stock" value="<%= t.getStock() %>" min="0"
                         style="width:70px">
                  <button type="submit" class="btn-sm btn-verde">✓</button>
                </form>
              </td>
              <td>
                <form action="<%= ctx %>/admin/productos" method="get">
                  <input type="hidden" name="accion"     value="eliminarTalla">
                  <input type="hidden" name="tallaId"    value="<%= t.getId() %>">
                  <input type="hidden" name="productoId" value="<%= p.getId() %>">
                  <% String confirmMsg = "¿Eliminar talla " + t.getTalla() + "?"; %>
                  <button type="submit" class="btn-sm btn-peligro"
                          onclick="return confirm('<%= confirmMsg %>')">✕</button>
                </form>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>

        <!-- Agregar nueva talla -->
        <form action="<%= ctx %>/admin/productos" method="post" class="form-agregar-talla">
          <input type="hidden" name="accion"     value="agregarTalla">
          <input type="hidden" name="productoId" value="<%= p.getId() %>">
          <input type="text"   name="talla"   placeholder="Talla (Ej: XL, 42)" required>
          <input type="number" name="stock"   placeholder="Stock" min="0" required>
          <button type="submit" class="btn-primario btn-sm">+ Agregar</button>
        </form>
      </div>
    </div>
    <% } %>

  </div>
</div>

<script>
function previsualizarImagenes(input) {
  var preview = document.getElementById('previewImagenes');
  preview.innerHTML = '';
  Array.from(input.files).forEach(function(file) {
    var reader = new FileReader();
    reader.onload = function(e) {
      var img = document.createElement('img');
      img.src = e.target.result;
      img.className = 'preview-img';
      preview.appendChild(img);
    };
    reader.readAsDataURL(file);
  });
}

function agregarFilaTalla() {
  var div = document.createElement('div');
  div.className = 'talla-nueva-fila';
  div.innerHTML = '<input type="text" name="tallaNombre" placeholder="Ej: L, 38" style="width:80px">'
    + '<input type="number" name="tallaStock" placeholder="Stock" min="0" style="width:80px">'
    + '<button type="button" onclick="this.parentElement.remove()" class="btn-sm btn-peligro">✕</button>';
  document.getElementById('tallasNuevas').appendChild(div);
}
</script>
</body>
</html>
