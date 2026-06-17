package modelo;

/**
 * Modelo que representa una imagen de un producto.
 * orden = 0 → imagen principal (portada)
 * orden > 0 → imágenes adicionales de galería
 */
public class ImagenProducto {

    private int    id;
    private int    productoId;
    private String rutaImagen;  // Ruta relativa: /uploads/productos/uuid.jpg
    private int    orden;

    // ─── Constructores ────────────────────────────────────

    public ImagenProducto() {}

    public ImagenProducto(int productoId, String rutaImagen, int orden) {
        this.productoId = productoId;
        this.rutaImagen = rutaImagen;
        this.orden      = orden;
    }

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getProductoId()                  { return productoId; }
    public void setProductoId(int p)            { this.productoId = p; }

    public String getRutaImagen()               { return rutaImagen; }
    public void setRutaImagen(String ruta)      { this.rutaImagen = ruta; }

    public int getOrden()                       { return orden; }
    public void setOrden(int orden)             { this.orden = orden; }

    /** true si esta imagen es la imagen principal */
    public boolean isPrincipal()                { return orden == 0; }

    @Override
    public String toString() {
        return "ImagenProducto{id=" + id + ", ruta=" + rutaImagen + ", orden=" + orden + "}";
    }
}
