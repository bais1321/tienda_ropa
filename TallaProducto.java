package modelo;

/**
 * Modelo que representa una talla específica de un producto,
 * con su propio stock independiente.
 *
 * Ejemplos de talla: S, M, L, XL (ropa) | 35, 36...42 (zapatos)
 */
public class TallaProducto {

    private int    id;
    private int    productoId;
    private String talla;
    private int    stock;

    // ─── Constructores ────────────────────────────────────

    public TallaProducto() {}

    public TallaProducto(int productoId, String talla, int stock) {
        this.productoId = productoId;
        this.talla      = talla;
        this.stock      = stock;
    }

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getProductoId()              { return productoId; }
    public void setProductoId(int p)        { this.productoId = p; }

    public String getTalla()                { return talla; }
    public void setTalla(String talla)      { this.talla = talla; }

    public int getStock()                   { return stock; }
    public void setStock(int stock)         { this.stock = stock; }

    /** true si esta talla tiene al menos 1 unidad disponible */
    public boolean isDisponible()           { return stock > 0; }

    /** true si el stock es bajo (entre 1 y 4 unidades) */
    public boolean isStockBajo()            { return stock > 0 && stock < 5; }

    @Override
    public String toString() {
        return "TallaProducto{id=" + id + ", talla=" + talla + ", stock=" + stock + "}";
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
