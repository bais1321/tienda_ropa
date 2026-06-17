package modelo;

import java.sql.Timestamp;

/**
 * Modelo que representa una notificación enviada (email o WhatsApp).
 * Se guarda en BD para historial y auditoría.
 */
public class Notificacion {

    public static final String TIPO_EMAIL     = "email";
    public static final String TIPO_WHATSAPP  = "whatsapp";
    public static final String ESTADO_ENVIADO = "enviado";
    public static final String ESTADO_FALLIDO = "fallido";

    private int       id;
    private int       pedidoId;
    private String    tipo;
    private String    destinatario;
    private String    asunto;
    private String    mensaje;
    private String    estado;
    private Timestamp fecha;

    // ─── Constructores ────────────────────────────────────

    public Notificacion() {}

    public Notificacion(int pedidoId, String tipo, String destinatario,
                        String asunto, String mensaje, String estado) {
        this.pedidoId     = pedidoId;
        this.tipo         = tipo;
        this.destinatario = destinatario;
        this.asunto       = asunto;
        this.mensaje      = mensaje;
        this.estado       = estado;
    }

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getPedidoId()                    { return pedidoId; }
    public void setPedidoId(int p)              { this.pedidoId = p; }

    public String getTipo()                     { return tipo; }
    public void setTipo(String tipo)            { this.tipo = tipo; }

    public String getDestinatario()             { return destinatario; }
    public void setDestinatario(String d)       { this.destinatario = d; }

    public String getAsunto()                   { return asunto; }
    public void setAsunto(String a)             { this.asunto = a; }

    public String getMensaje()                  { return mensaje; }
    public void setMensaje(String m)            { this.mensaje = m; }

    public String getEstado()                   { return estado; }
    public void setEstado(String e)             { this.estado = e; }

    public Timestamp getFecha()                 { return fecha; }
    public void setFecha(Timestamp f)           { this.fecha = f; }

    public boolean isEmail()                    { return TIPO_EMAIL.equals(tipo); }
    public boolean isWhatsapp()                 { return TIPO_WHATSAPP.equals(tipo); }
    public boolean fueEnviada()                 { return ESTADO_ENVIADO.equals(estado); }

    @Override
    public String toString() {
        return "Notificacion{pedido=" + pedidoId + ", tipo=" + tipo
                + ", estado=" + estado + "}";
    }
}
