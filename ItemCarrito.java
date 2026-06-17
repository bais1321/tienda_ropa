package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Modelo que representa un ítem en el carrito de compras.
 * El carrito vive en la HttpSession del usuario (no en BD).
 * Se serializa automáticamente por Tomcat.
 *
 * Contiene datos del producto y talla seleccionada,
 * más el precio congelado en el momento de agregar.
 */
public class ItemCarrito implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int        productoId;
    private String     nombreProducto;
    private int        tallaId;
    private String     talla;
    private int        cantidad;
    private BigDecimal precioUnitario;   // Precio CON descuento del producto, congelado
    private int        descuentoAplicado; // % de descuento del producto al momento de agregar
    private String     imagenPrincipal;  // Ruta imagen para mostrar en carrito
    private int        stockDisponible;  // Stock de esa talla al momento de agregar

    // ─── Constructores ────────────────────────────────────

    public ItemCarrito() {}

    public ItemCarrito(int productoId, String nombreProducto,
                       int tallaId, String talla,
                       int cantidad, BigDecimal precioUnitario,
                       int descuentoAplicado, String imagenPrincipal,
                       int stockDisponible) {
        this.productoId       = productoId;
        this.nombreProducto   = nombreProducto;
        this.tallaId          = tallaId;
        this.talla            = talla;
        this.cantidad         = cantidad;
        this.precioUnitario   = precioUnitario;
        this.descuentoAplicado = descuentoAplicado;
        this.imagenPrincipal  = imagenPrincipal;
        this.stockDisponible  = stockDisponible;
    }

    // ─── Getters y Setters ────────────────────────────────

    public int getProductoId()                      { return productoId; }
    public void setProductoId(int p)                { this.productoId = p; }

    public String getNombreProducto()               { return nombreProducto; }
    public void setNombreProducto(String n)         { this.nombreProducto = n; }

    public int getTallaId()                         { return tallaId; }
    public void setTallaId(int t)                   { this.tallaId = t; }

    public String getTalla()                        { return talla; }
    public void setTalla(String t)                  { this.talla = t; }

    public int getCantidad()                        { return cantidad; }
    public void setCantidad(int c)                  { this.cantidad = c; }

    public BigDecimal getPrecioUnitario()           { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p)     { this.precioUnitario = p; }

    public int getDescuentoAplicado()               { return descuentoAplicado; }
    public void setDescuentoAplicado(int d)         { this.descuentoAplicado = d; }

    public String getImagenPrincipal()              { return imagenPrincipal; }
    public void setImagenPrincipal(String i)        { this.imagenPrincipal = i; }

    public int getStockDisponible()                 { return stockDisponible; }
    public void setStockDisponible(int s)           { this.stockDisponible = s; }

    // ─── Métodos calculados ───────────────────────────────

    /** Subtotal de esta línea: precioUnitario × cantidad */
    public BigDecimal getSubtotalLinea() {
        return precioUnitario.multiply(new BigDecimal(cantidad))
                             .setScale(2, RoundingMode.HALF_UP);
    }

    /** Clave única para identificar un ítem en el carrito: productoId + tallaId */
    public String getClave() {
        return productoId + "_" + tallaId;
    }

    /**
     * Verifica si la cantidad actual supera el stock disponible.
     * Útil para mostrar advertencias en el carrito.
     */
    public boolean exceedeStock() {
        return cantidad > stockDisponible;
    }

    @Override
    public String toString() {
        return "ItemCarrito{producto=" + productoId + ", talla=" + talla
                + ", cantidad=" + cantidad + ", precio=" + precioUnitario + "}";
    }
}
