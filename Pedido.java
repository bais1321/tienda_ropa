package modelo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un pedido.
 * Un pedido puede ser de un usuario registrado (usuarioId != 0)
 * o de un invitado (usuarioId = 0, datos en campos *Cliente).
 */
public class Pedido {

    // Estados posibles
    public static final String ESTADO_PENDIENTE   = "pendiente";
    public static final String ESTADO_AUTORIZADO  = "autorizado";
    public static final String ESTADO_PREPARADO   = "preparado";
    public static final String ESTADO_EN_CAMINO   = "en_camino";
    public static final String ESTADO_ENTREGADO   = "entregado";
    public static final String ESTADO_CANCELADO   = "cancelado";

    // Tipos de pago
    public static final String PAGO_EFECTIVO       = "efectivo";
    public static final String PAGO_TRANSFERENCIA  = "transferencia";
    public static final String PAGO_LINEA          = "linea";

    // Tipos de entrega
    public static final String ENTREGA_PERSONAL    = "personal";
    public static final String ENTREGA_DOMICILIO   = "domicilio";

    private int        id;
    private String     codigoPedido;
    private Integer    usuarioId;          // null si es invitado
    private String     nombreCliente;
    private String     apellidoCliente;
    private String     telefonoCliente;
    private String     emailCliente;
    private BigDecimal subtotal;
    private BigDecimal descuentoPrimeraCompra;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String     tipoPago;
    private String     tipoEntrega;
    private String     direccion;
    private String     estado;
    private String     notaAdmin;
    private Timestamp  fecha;
    private Timestamp  fechaActualizacion;

    // Relación — detalles del pedido
    private List<DetallePedido> detalles = new ArrayList<>();

    // ─── Constructores ────────────────────────────────────

    public Pedido() {}

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public String getCodigoPedido()                 { return codigoPedido; }
    public void setCodigoPedido(String c)           { this.codigoPedido = c; }

    public Integer getUsuarioId()                   { return usuarioId; }
    public void setUsuarioId(Integer u)             { this.usuarioId = u; }

    public String getNombreCliente()                { return nombreCliente; }
    public void setNombreCliente(String n)          { this.nombreCliente = n; }

    public String getApellidoCliente()              { return apellidoCliente; }
    public void setApellidoCliente(String a)        { this.apellidoCliente = a; }

    public String getNombreCompletoCliente()        { return nombreCliente + " " + apellidoCliente; }

    public String getTelefonoCliente()              { return telefonoCliente; }
    public void setTelefonoCliente(String t)        { this.telefonoCliente = t; }

    public String getEmailCliente()                 { return emailCliente; }
    public void setEmailCliente(String e)           { this.emailCliente = e; }

    public BigDecimal getSubtotal()                 { return subtotal; }
    public void setSubtotal(BigDecimal s)           { this.subtotal = s; }

    public BigDecimal getDescuentoPrimeraCompra()   { return descuentoPrimeraCompra; }
    public void setDescuentoPrimeraCompra(BigDecimal d){ this.descuentoPrimeraCompra = d; }

    public BigDecimal getCostoEnvio()               { return costoEnvio; }
    public void setCostoEnvio(BigDecimal c)         { this.costoEnvio = c; }

    public BigDecimal getTotal()                    { return total; }
    public void setTotal(BigDecimal t)              { this.total = t; }

    public String getTipoPago()                     { return tipoPago; }
    public void setTipoPago(String t)               { this.tipoPago = t; }

    public String getTipoEntrega()                  { return tipoEntrega; }
    public void setTipoEntrega(String t)            { this.tipoEntrega = t; }

    public String getDireccion()                    { return direccion; }
    public void setDireccion(String d)              { this.direccion = d; }

    public String getEstado()                       { return estado; }
    public void setEstado(String e)                 { this.estado = e; }

    public String getNotaAdmin()                    { return notaAdmin; }
    public void setNotaAdmin(String n)              { this.notaAdmin = n; }

    public Timestamp getFecha()                     { return fecha; }
    public void setFecha(Timestamp f)               { this.fecha = f; }

    public Timestamp getFechaActualizacion()        { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp f)  { this.fechaActualizacion = f; }

    public List<DetallePedido> getDetalles()        { return detalles; }
    public void setDetalles(List<DetallePedido> d)  { this.detalles = d; }

    // ─── Métodos de conveniencia ──────────────────────────

    public boolean esDeInvitado()           { return usuarioId == null || usuarioId == 0; }
    public boolean esPorTransferencia()     { return PAGO_TRANSFERENCIA.equals(tipoPago); }
    public boolean esADomicilio()           { return ENTREGA_DOMICILIO.equals(tipoEntrega); }
    public boolean esCancelado()            { return ESTADO_CANCELADO.equals(estado); }
    public boolean esEntregado()            { return ESTADO_ENTREGADO.equals(estado); }

    /** Etiqueta legible del estado para mostrar en vistas */
    public String getEstadoLabel() {
        switch (estado != null ? estado : "") {
            case ESTADO_PENDIENTE:  return "Pendiente";
            case ESTADO_AUTORIZADO: return "Autorizado";
            case ESTADO_PREPARADO:  return "Preparado";
            case ESTADO_EN_CAMINO:  return "En camino";
            case ESTADO_ENTREGADO:  return "Entregado";
            case ESTADO_CANCELADO:  return "Cancelado";
            default:                return estado;
        }
    }

    /** Clase CSS correspondiente al estado (para badges de colores) */
    public String getEstadoCss() {
        switch (estado != null ? estado : "") {
            case ESTADO_PENDIENTE:  return "badge-gris";
            case ESTADO_AUTORIZADO: return "badge-azul";
            case ESTADO_PREPARADO:  return "badge-amarillo";
            case ESTADO_EN_CAMINO:  return "badge-morado";
            case ESTADO_ENTREGADO:  return "badge-verde";
            case ESTADO_CANCELADO:  return "badge-rojo";
            default:                return "badge-gris";
        }
    }

    @Override
    public String toString() {
        return "Pedido{id=" + id + ", codigo=" + codigoPedido
                + ", estado=" + estado + ", total=" + total + "}";
    }
}
