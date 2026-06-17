package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Modelo que representa una línea del detalle de un pedido.
 * El precio y descuento están congelados al momento de la compra,
 * por lo que cambios futuros en el producto no afectan este registro.
 */
public class DetallePedido {

    private int        id;
    private int        pedidoId;
    private int        productoId;
    private int        tallaId;
    private int        cantidad;
    private BigDecimal precioUnitario;    // Precio congelado con descuento aplicado
    private int        descuentoAplicado; // % de descuento en el momento de la compra

    // Campos extra — se llenan desde las vistas de BD para mostrar en UI
    private String     nombreProducto;
    private String     talla;
    private String     imagenProducto;

    // ─── Constructores ────────────────────────────────────

    public DetallePedido() {}

    public DetallePedido(int pedidoId, int productoId, int tallaId,
                         int cantidad, BigDecimal precioUnitario, int descuentoAplicado) {
        this.pedidoId         = pedidoId;
        this.productoId       = productoId;
        this.tallaId          = tallaId;
        this.cantidad         = cantidad;
        this.precioUnitario   = precioUnitario;
        this.descuentoAplicado = descuentoAplicado;
    }

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public int getPedidoId()                        { return pedidoId; }
    public void setPedidoId(int p)                  { this.pedidoId = p; }

    public int getProductoId()                      { return productoId; }
    public void setProductoId(int p)                { this.productoId = p; }

    public int getTallaId()                         { return tallaId; }
    public void setTallaId(int t)                   { this.tallaId = t; }

    public int getCantidad()                        { return cantidad; }
    public void setCantidad(int c)                  { this.cantidad = c; }

    public BigDecimal getPrecioUnitario()           { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p)     { this.precioUnitario = p; }

    public int getDescuentoAplicado()               { return descuentoAplicado; }
    public void setDescuentoAplicado(int d)         { this.descuentoAplicado = d; }

    public String getNombreProducto()               { return nombreProducto; }
    public void setNombreProducto(String n)         { this.nombreProducto = n; }

    public String getTalla()                        { return talla; }
    public void setTalla(String t)                  { this.talla = t; }

    public String getImagenProducto()               { return imagenProducto; }
    public void setImagenProducto(String i)         { this.imagenProducto = i; }

    // ─── Métodos calculados ───────────────────────────────

    /** Subtotal de esta línea = precioUnitario × cantidad */
    public BigDecimal getSubtotalLinea() {
        return precioUnitario.multiply(new BigDecimal(cantidad))
                             .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "DetallePedido{producto=" + productoId + ", talla=" + talla
                + ", cantidad=" + cantidad + ", precio=" + precioUnitario + "}";
    }
}
