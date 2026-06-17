package servlet.admin;

import dao.ConfigDAO;
import modelo.ConfigTienda;
import util.SubidaImagenUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/admin/config")
@MultipartConfig(maxFileSize = 2 * 1024 * 1024)
public class ConfigServlet extends HttpServlet {

    private ConfigDAO configDAO = new ConfigDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("config", configDAO.obtener());
        req.getRequestDispatcher("/jsp/admin/config.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        ConfigTienda c = new ConfigTienda();
        c.setNombreTienda(req.getParameter("nombreTienda").trim());
        c.setNumeroWhatsapp(req.getParameter("numeroWhatsapp").trim());
        c.setDatosBancarios(req.getParameter("datosBancarios"));
        c.setCostoEnvioDomicilio(new BigDecimal(req.getParameter("costoEnvio")));
        c.setMinimoDomicilio(new BigDecimal(req.getParameter("minimoDomicilio")));
        c.setEmailNotificaciones(req.getParameter("emailNotificaciones"));
        c.setSmtpHost(req.getParameter("smtpHost"));
        c.setSmtpPuerto(Integer.parseInt(req.getParameter("smtpPuerto")));
        c.setSmtpPassword(req.getParameter("smtpPassword")); // vacío = no cambiar

        // Subir nuevo logo si se envió
        Part logoFile = req.getPart("logo");
        if (logoFile != null && logoFile.getSize() > 0) {
            try {
                String rutaLogo = SubidaImagenUtil.subirImagen(
                        logoFile, req.getServletContext().getRealPath("/"));
                c.setUrlLogo(rutaLogo);
            } catch (Exception e) {
                req.setAttribute("error", "Error al subir el logo: " + e.getMessage());
                req.setAttribute("config", c);
                req.getRequestDispatcher("/jsp/admin/config.jsp").forward(req, resp);
                return;
            }
        } else {
            c.setUrlLogo(configDAO.obtener().getUrlLogo()); // Mantener logo actual
        }

        boolean ok = configDAO.guardar(c);
        req.getSession().setAttribute("toast",
                ok ? "Configuración guardada exitosamente." : "Error al guardar la configuración.");
        resp.sendRedirect(req.getContextPath() + "/admin/config");
    }
}
