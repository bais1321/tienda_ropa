package modelo;

import java.math.BigDecimal;

/**
 * Modelo que representa la configuración global de la tienda.
 * Siempre existe una sola fila (id = 1) en la BD.
 * El admin puede editar estos valores desde el panel.
 */
public class ConfigTienda {

    private int        id;
    private String     nombreTienda;
    private String     urlLogo;
    private String     numeroWhatsapp;
    private String     datosBancarios;
    private BigDecimal costoEnvioDomicilio;
    private BigDecimal minimoDomicilio;
    private String     emailNotificaciones;
    private String     smtpHost;
    private int        smtpPuerto;
    private String     smtpPassword;

    // ─── Constructores ────────────────────────────────────

    public ConfigTienda() {}

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }

    public String getNombreTienda()                     { return nombreTienda; }
    public void setNombreTienda(String n)               { this.nombreTienda = n; }

    public String getUrlLogo()                          { return urlLogo; }
    public void setUrlLogo(String u)                    { this.urlLogo = u; }

    public String getNumeroWhatsapp()                   { return numeroWhatsapp; }
    public void setNumeroWhatsapp(String n)             { this.numeroWhatsapp = n; }

    public String getDatosBancarios()                   { return datosBancarios; }
    public void setDatosBancarios(String d)             { this.datosBancarios = d; }

    public BigDecimal getCostoEnvioDomicilio()          { return costoEnvioDomicilio; }
    public void setCostoEnvioDomicilio(BigDecimal c)    { this.costoEnvioDomicilio = c; }

    public BigDecimal getMinimoDomicilio()              { return minimoDomicilio; }
    public void setMinimoDomicilio(BigDecimal m)        { this.minimoDomicilio = m; }

    public String getEmailNotificaciones()              { return emailNotificaciones; }
    public void setEmailNotificaciones(String e)        { this.emailNotificaciones = e; }

    public String getSmtpHost()                         { return smtpHost; }
    public void setSmtpHost(String s)                   { this.smtpHost = s; }

    public int getSmtpPuerto()                          { return smtpPuerto; }
    public void setSmtpPuerto(int p)                    { this.smtpPuerto = p; }

    public String getSmtpPassword()                     { return smtpPassword; }
    public void setSmtpPassword(String p)               { this.smtpPassword = p; }

    // ─── Métodos de conveniencia ──────────────────────────

    /**
     * Genera el enlace de WhatsApp para que el cliente envíe su comprobante.
     * @param codigoPedido  Código del pedido (ej: T-00123)
     * @param nombreCliente Nombre del cliente para personalizar el mensaje
     */
    public String getLinkWhatsappComprobante(String codigoPedido, String nombreCliente) {
        String mensaje = "Hola, mi nombre es " + nombreCliente
                + ". Quiero enviar el comprobante de pago de mi pedido "
                + codigoPedido + ".";
        return "https://wa.me/" + numeroWhatsapp
                + "?text=" + encodeUrlComponent(mensaje);
    }

    /**
     * Genera el enlace de WhatsApp para que el admin notifique al cliente.
     * @param telefonoCliente Teléfono del cliente (con código de país)
     * @param mensaje         Mensaje a enviar
     */
    public String getLinkWhatsappNotificacion(String telefonoCliente, String mensaje) {
        return "https://wa.me/" + telefonoCliente
                + "?text=" + encodeUrlComponent(mensaje);
    }

    /** Codificación básica de URL para el texto del mensaje de WhatsApp */
    private String encodeUrlComponent(String text) {
        return text.replace(" ", "%20")
                   .replace(",", "%2C")
                   .replace(".", "%2E")
                   .replace("!", "%21")
                   .replace("¡", "%C2%A1")
                   .replace(":", "%3A")
                   .replace("\n", "%0A");
    }

    @Override
    public String toString() {
        return "ConfigTienda{nombre=" + nombreTienda
                + ", whatsapp=" + numeroWhatsapp + "}";
    }
}
