package util;

import modelo.ConfigTienda;
import modelo.Pedido;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Utilidad para enviar correos con JavaMail.
 *
 * LIBRERÍAS NECESARIAS (en WEB-INF/lib/):
 *   - javax.mail.jar  (JavaMail API)
 *   - activation.jar  (JAF)
 *   Descarga desde: https://javaee.github.io/javamail/
 *
 * CONFIGURACIÓN EN BD:
 *   La configuración SMTP se lee de la tabla configuracion_tienda.
 *   Para Gmail: necesitas una "App Password" de Google, no tu contraseña normal.
 *   Ve a: Google Account → Security → App Passwords
 */
public class MailUtil {

    /**
     * Envía un correo HTML usando la configuración de la tienda.
     *
     * @param config       Configuración de la tienda (SMTP, email origen)
     * @param destinatario Email del destinatario
     * @param asunto       Asunto del correo
     * @param cuerpoHtml   Cuerpo del correo en HTML
     * @return true si el correo fue enviado exitosamente
     */
    public static boolean enviar(ConfigTienda config, String destinatario,
                                  String asunto, String cuerpoHtml) {
        if (config.getEmailNotificaciones() == null || config.getSmtpPassword() == null) {
            System.err.println("⚠ MailUtil: Configuración SMTP incompleta en la BD.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            config.getSmtpHost());
        props.put("mail.smtp.port",            String.valueOf(config.getSmtpPuerto()));
        props.put("mail.smtp.ssl.trust",       config.getSmtpHost());

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    config.getEmailNotificaciones(),
                    config.getSmtpPassword()
                );
            }
        });

        try {
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(
                config.getEmailNotificaciones(),
                config.getNombreTienda()
            ));
            mensaje.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setContent(cuerpoHtml, "text/html; charset=UTF-8");

            Transport.send(mensaje);
            System.out.println("✅ Correo enviado a: " + destinatario);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo a " + destinatario + ": " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────
    //  Plantillas de correo por evento
    // ─────────────────────────────────────────────────────

    /**
     * Correo de confirmación al crear un pedido nuevo.
     */
    public static String plantillaPedidoCreado(Pedido p, ConfigTienda config) {
        String instrucciones = "";
        if (Pedido.PAGO_TRANSFERENCIA.equals(p.getTipoPago())) {
            String linkWA = config.getLinkWhatsappComprobante(
                p.getCodigoPedido(), p.getNombreCompletoCliente());
            instrucciones = "<div style='background:#FFF9E6;border-left:4px solid #F5C518;"
                    + "padding:16px;margin:16px 0;border-radius:4px;'>"
                    + "<strong>📸 Envía tu comprobante de transferencia</strong><br><br>"
                    + config.getDatosBancarios().replace("\n","<br>")
                    + "<br><br>"
                    + "<a href='" + linkWA + "' style='background:#25D366;color:white;"
                    + "padding:10px 20px;border-radius:4px;text-decoration:none;"
                    + "font-weight:bold;display:inline-block;margin-top:8px;'>"
                    + "📱 Enviar comprobante por WhatsApp</a>"
                    + "</div>";
        } else {
            instrucciones = "<div style='background:#f0f4ff;border-left:4px solid #1A3C5E;"
                    + "padding:16px;margin:16px 0;border-radius:4px;'>"
                    + "Pago en efectivo al momento de la entrega."
                    + "</div>";
        }

        return construirPlantillaBase(config.getNombreTienda(),
            "Pedido recibido — " + p.getCodigoPedido(),
            "Hola, <strong>" + p.getNombreCompletoCliente() + "</strong>",
            "Tu pedido ha sido recibido correctamente.",
            resumenPedido(p) + instrucciones,
            "Rastrear mi pedido",
            "#/rastreo?codigo=" + p.getCodigoPedido()
        );
    }

    public static String plantillaCambioEstado(Pedido p, ConfigTienda config) {
        String mensaje;
        switch (p.getEstado()) {
            case Pedido.ESTADO_AUTORIZADO:
                mensaje = "✅ Tu pago fue confirmado. ¡Estamos preparando tu pedido!";
                break;
            case Pedido.ESTADO_PREPARADO:
                mensaje = "📦 Tu pedido está listo y esperando ser enviado.";
                break;
            case Pedido.ESTADO_EN_CAMINO:
                String nota = (p.getNotaAdmin() != null && !p.getNotaAdmin().isEmpty())
                        ? "<br><br><strong>Detalles de entrega:</strong><br>" + p.getNotaAdmin()
                        : "";
                mensaje = (Pedido.ENTREGA_DOMICILIO.equals(p.getTipoEntrega()))
                        ? "🚚 Tu pedido está en camino a tu dirección." + nota
                        : "📍 Tu pedido está listo para recoger." + nota;
                break;
            case Pedido.ESTADO_ENTREGADO:
                mensaje = "🎉 ¡Tu pedido fue entregado! Gracias por tu compra. ¡Esperamos verte pronto!";
                break;
            case Pedido.ESTADO_CANCELADO:
                mensaje = "❌ Tu pedido fue cancelado. Si tienes dudas, contáctanos por WhatsApp.";
                break;
            default:
                mensaje = "El estado de tu pedido ha sido actualizado a: " + p.getEstadoLabel();
        }

        return construirPlantillaBase(config.getNombreTienda(),
            "Actualización de pedido — " + p.getCodigoPedido(),
            "Hola, <strong>" + p.getNombreCompletoCliente() + "</strong>",
            "Estado actualizado: <strong>" + p.getEstadoLabel() + "</strong>",
            "<p>" + mensaje + "</p>" + resumenPedidoSimple(p),
            "Ver mi pedido",
            "#/rastreo?codigo=" + p.getCodigoPedido()
        );
    }

    // ─────────────────────────────────────────────────────
    //  Helpers privados
    // ─────────────────────────────────────────────────────

    private static String resumenPedido(Pedido p) {
        return "<table style='width:100%;border-collapse:collapse;margin:16px 0;font-size:14px;'>"
             + "<tr style='background:#1A1A1A;color:white;'>"
             + "<td style='padding:8px;'>Código</td>"
             + "<td style='padding:8px;'>Entrega</td>"
             + "<td style='padding:8px;'>Pago</td>"
             + "<td style='padding:8px;text-align:right;'>Total</td></tr>"
             + "<tr style='background:#F2F2F2;'>"
             + "<td style='padding:8px;'><strong>" + p.getCodigoPedido() + "</strong></td>"
             + "<td style='padding:8px;'>" + (Pedido.ENTREGA_DOMICILIO.equals(p.getTipoEntrega()) ? "Domicilio" : "Personal") + "</td>"
             + "<td style='padding:8px;'>" + p.getTipoPago() + "</td>"
             + "<td style='padding:8px;text-align:right;'><strong>Q " + p.getTotal() + "</strong></td>"
             + "</tr></table>";
    }

    private static String resumenPedidoSimple(Pedido p) {
        return "<p style='color:#666;font-size:13px;'>Pedido: <strong>"
             + p.getCodigoPedido() + "</strong> | Total: <strong>Q "
             + p.getTotal() + "</strong></p>";
    }

    private static String construirPlantillaBase(String nombreTienda, String asuntoInterno,
            String saludo, String subtitulo, String contenido,
            String textoBoton, String linkBoton) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='"
             + "font-family:Arial,sans-serif;background:#F2F2F2;margin:0;padding:20px;'>"
             + "<div style='max-width:600px;margin:0 auto;background:white;"
             + "border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>"
             + "<div style='background:#1A1A1A;padding:24px;text-align:center;'>"
             + "<h1 style='color:#F5C518;margin:0;font-size:24px;'>" + nombreTienda + "</h1>"
             + "</div>"
             + "<div style='padding:32px;'>"
             + "<h2 style='color:#1A1A1A;margin-top:0;'>" + saludo + "</h2>"
             + "<p style='color:#444;font-size:16px;'>" + subtitulo + "</p>"
             + contenido
             + "<div style='text-align:center;margin-top:24px;'>"
             + "<a href='" + linkBoton + "' style='background:#F5C518;color:#1A1A1A;"
             + "padding:12px 28px;border-radius:4px;text-decoration:none;"
             + "font-weight:bold;font-size:15px;display:inline-block;'>"
             + textoBoton + "</a>"
             + "</div></div>"
             + "<div style='background:#F2F2F2;padding:16px;text-align:center;"
             + "color:#888;font-size:12px;'>"
             + "© " + nombreTienda + " — Este es un correo automático, no responder."
             + "</div></div></body></html>";
    }
}
