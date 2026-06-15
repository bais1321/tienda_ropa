// ============================================================
//  filtros.js — Filtros del catálogo
// ============================================================

// Auto-submit del formulario de filtros al cambiar un campo
document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('formFiltros');
  if (!form) return;

  // Mantener valores ocultos consistentes al cambiar filtros
  const radiosCategoria    = form.querySelectorAll('input[name="categoria"]');
  const radiosSubcategoria = form.querySelectorAll('input[name="subcategoria"]');
  const checkDescuento     = form.querySelector('input[name="descuento"]');

  // Eliminar inputs hidden duplicados antes de enviar
  form.addEventListener('submit', function () {
    const hiddenCat = form.querySelectorAll('input[type="hidden"][name="categoria"]');
    const hiddenSub = form.querySelectorAll('input[type="hidden"][name="subcategoria"]');

    // Si hay radio activo, eliminar el hidden para no duplicar
    radiosCategoria.forEach(r => { if (r.checked && r.value) hiddenCat.forEach(h => h.remove()); });
    radiosSubcategoria.forEach(r => { if (r.checked && r.value) hiddenSub.forEach(h => h.remove()); });
  });
});
