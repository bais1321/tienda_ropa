// ============================================================
//  carrito.js — Lógica del carrito y búsqueda en tiempo real
// ============================================================

// ─── Actualizar badge del carrito ──────────────────────────
function actualizarBadgeCarrito(cantidad) {
  const badge = document.getElementById('badgeCarrito');
  if (!badge) return;
  if (cantidad > 0) {
    badge.textContent = cantidad;
    badge.classList.remove('oculto');
    badge.style.animation = 'none';
    void badge.offsetWidth; // Forzar reflow
    badge.style.animation = 'pulso 0.4s ease';
  } else {
    badge.classList.add('oculto');
  }
}

// ─── Búsqueda en tiempo real ───────────────────────────────
let timeoutBusqueda = null;

function buscarEnTiempoReal(query) {
  const contenedor = document.getElementById('resultadosBusqueda');
  if (!contenedor) return;

  clearTimeout(timeoutBusqueda);

  if (query.trim().length < 2) {
    contenedor.classList.add('oculto');
    contenedor.innerHTML = '';
    return;
  }

  timeoutBusqueda = setTimeout(() => {
    // Redirigir al catálogo con búsqueda
    const ctx = getContextPath();
    fetch(ctx + '/catalogo?q=' + encodeURIComponent(query.trim()) + '&ajax=true')
      .then(r => r.text())
      .then(html => {
        // Como no tenemos endpoint AJAX, simplemente mostramos
        // el link de búsqueda como resultado
        contenedor.innerHTML = `
          <div class="resultado-item" onclick="window.location='${ctx}/catalogo?q=${encodeURIComponent(query.trim())}'">
            <div class="resultado-item-info">
              <p>🔍 Buscar "<strong>${escapeHtml(query)}</strong>"</p>
              <span>Ver todos los resultados en el catálogo</span>
            </div>
          </div>`;
        contenedor.classList.remove('oculto');
      })
      .catch(() => {
        // Si falla, solo mostrar el link
        contenedor.innerHTML = `
          <a href="${ctx}/catalogo?q=${encodeURIComponent(query.trim())}" class="resultado-item">
            <div class="resultado-item-info">
              <p>🔍 Buscar "<strong>${escapeHtml(query)}</strong>"</p>
            </div>
          </a>`;
        contenedor.classList.remove('oculto');
      });
  }, 300);
}

// Cerrar resultados al hacer clic fuera
document.addEventListener('click', function(e) {
  const contenedor = document.getElementById('resultadosBusqueda');
  const input = document.getElementById('inputBusqueda');
  if (contenedor && input && !input.contains(e.target) && !contenedor.contains(e.target)) {
    contenedor.classList.add('oculto');
  }
});

// Buscar al presionar Enter
document.addEventListener('DOMContentLoaded', function() {
  const input = document.getElementById('inputBusqueda');
  if (input) {
    input.addEventListener('keydown', function(e) {
      if (e.key === 'Enter' && this.value.trim()) {
        window.location = getContextPath() + '/catalogo?q=' + encodeURIComponent(this.value.trim());
      }
    });
  }
});

// ─── Toggle menú móvil ─────────────────────────────────────
function toggleMenu() {
  const links = document.querySelector('.nav-links');
  const acc   = document.querySelector('.nav-acciones');
  if (links) links.style.display = links.style.display === 'flex' ? 'none' : 'flex';
  if (acc)   acc.style.display   = acc.style.display   === 'flex' ? 'none' : 'flex';
}

// ─── Helpers ───────────────────────────────────────────────
function getContextPath() {
  // Detecta el context path de la aplicación
  const base = document.querySelector('base');
  if (base) return base.href.replace(window.location.origin, '').replace(/\/$/, '');
  const path = window.location.pathname;
  const parts = path.split('/');
  return parts.length > 1 ? '/' + parts[1] : '';
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.appendChild(document.createTextNode(text));
  return div.innerHTML;
}

// ─── Toast manual desde JS ─────────────────────────────────
function mostrarToast(mensaje, tipo = 'exito') {
  const t = document.createElement('div');
  t.className = `toast toast-${tipo}`;
  t.textContent = (tipo === 'exito' ? '✓ ' : '✕ ') + mensaje;
  document.body.appendChild(t);
  setTimeout(() => {
    t.classList.add('toast-salir');
    setTimeout(() => t.remove(), 400);
  }, 3500);
}
