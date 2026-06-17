package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un producto del catálogo.
 * Incluye sus tallas (lista de TallaProducto) e imágenes.
 */
public class Producto {

    // Estados posibles del producto
    public static final String ESTADO_ACTIVO   = "activo";
    public static final String ESTADO_AGOTADO  = "agotado";
    public static final String ESTADO_INACTIVO = "inactivo";

    private int        id;
    private String     nombre;
    private String     descripcion;
    private BigDecimal precioOriginal;
    private int        descuento;         // Porcentaje 0-99
    private String     categoria;         // "ropa" o "zapatos"
    private String     subcategoria;      // "hombre" o "mujer"
    private boolean    esDestacado;
    private String     estado;

    // Relaciones — se cargan según necesidad
    private List<TallaProducto>   tallas   = new ArrayList<>();
    private List<ImagenProducto>  imagenes = new ArrayList<>();

    // Campo calculado — stock total (suma de todas las tallas)
    private int stockTotal;

    // ─── Constructores ────────────────────────────────────

    public Producto() {}

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getNombre()                   { return nombre; }
    public void setNombre(String nombre)        { this.nombre = nombre; }

    public String getDescripcion()              { return descripcion; }
    public void setDescripcion(String d)        { this.descripcion = d; }

    public BigDecimal getPrecioOriginal()       { return precioOriginal; }
    public void setPrecioOriginal(BigDecimal p) { this.precioOriginal = p; }

    public int getDescuento()                   { return descuento; }
    public void setDescuento(int d)             { this.descuento = d; }

    public String getCategoria()                { return categoria; }
    public void setCategoria(String c)          { this.categoria = c; }

    public String getSubcategoria()             { return subcategoria; }
    public void setSubcategoria(String s)       { this.subcategoria = s; }

    public boolean isEsDestacado()              { return esDestacado; }
    public void setEsDestacado(boolean e)       { this.esDestacado = e; }

    public String getEstado()                   { return estado; }
    public void setEstado(String e)             { this.estado = e; }

    public List<TallaProducto> getTallas()      { return tallas; }
    public void setTallas(List<TallaProducto> t){ this.tallas = t; }

    public List<ImagenProducto> getImagenes()   { return imagenes; }
    public void setImagenes(List<ImagenProducto> i){ this.imagenes = i; }

    public int getStockTotal()                  { return stockTotal; }
    public void setStockTotal(int s)            { this.stockTotal = s; }

    // ─── Métodos calculados ───────────────────────────────

    /**
     * Precio final después de aplicar el % de descuento del producto.
     * Ejemplo: precio=Q200, descuento=15% → precio_final=Q170.00
     */
    public BigDecimal getPrecioFinal() {
        if (descuento <= 0) return precioOriginal;
        BigDecimal factor = BigDecimal.ONE
                .subtract(new BigDecimal(descuento).divide(new BigDecimal(100)));
        return precioOriginal.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /** true si el producto tiene algún descuento configurado */
    public boolean tieneDescuento() {
        return descuento > 0;
    }

    /** true si el producto está activo y tiene stock > 0 */
    public boolean isDisponible() {
        return ESTADO_ACTIVO.equals(estado) && stockTotal > 0;
    }

    /** true si el stock total es bajo (menos de 5 unidades en total) */
    public boolean isStockBajo() {
        return stockTotal > 0 && stockTotal < 5;
    }

    /**
     * Devuelve la imagen principal (orden = 0).
     * Si no hay imágenes, devuelve null.
     */
    public ImagenProducto getImagenPrincipal() {
        if (imagenes == null || imagenes.isEmpty()) return null;
        for (ImagenProducto img : imagenes) {
            if (img.getOrden() == 0) return img;
        }
        return imagenes.get(0);
    }

    /** Ruta de la imagen principal o imagen placeholder */
    public String getRutaImagenPrincipal() {
        ImagenProducto img = getImagenPrincipal();
        return img != null ? img.getRutaImagen() : "/img/placeholder.jpg";
    }

    /**
     * Devuelve el stock disponible de una talla específica.
     * Devuelve 0 si la talla no existe.
     */
    public int getStockDeTalla(int tallaId) {
        for (TallaProducto t : tallas) {
            if (t.getId() == tallaId) return t.getStock();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Producto{id=" + id + ", nombre=" + nombre
                + ", precio=" + getPrecioFinal() + ", estado=" + estado + "}";
    }
}
