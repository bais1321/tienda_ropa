package servlet;

import dao.UsuarioDAO;
import modelo.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Pre-llenar datos si viene de post-compra como invitado
        req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String nombre    = req.getParameter("nombre");
        String apellido  = req.getParameter("apellido");
        String telefono  = req.getParameter("telefono");
        String email     = req.getParameter("email");
        String password  = req.getParameter("password");
        String confirmar = req.getParameter("confirmar");

        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()
                || apellido == null || apellido.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            req.setAttribute("error", "Todos los campos obligatorios deben completarse.");
            poblarFormulario(req, nombre, apellido, telefono, email);
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirmar)) {
            req.setAttribute("error", "Las contraseñas no coinciden.");
            poblarFormulario(req, nombre, apellido, telefono, email);
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
            return;
        }

        if (password.length() < 6) {
            req.setAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            poblarFormulario(req, nombre, apellido, telefono, email);
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
            return;
        }

        String emailLower = email.trim().toLowerCase();
        if (usuarioDAO.emailExiste(emailLower)) {
            req.setAttribute("error", "Este correo ya está registrado. ¿Deseas iniciar sesión?");
            poblarFormulario(req, nombre, apellido, telefono, email);
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
            return;
        }

        Usuario nuevo = new Usuario(nombre.trim(), apellido.trim(),
                telefono != null ? telefono.trim() : null, emailLower, password);

        boolean ok = usuarioDAO.registrar(nuevo);
        if (!ok) {
            req.setAttribute("error", "Error al crear la cuenta. Por favor intenta nuevamente.");
            poblarFormulario(req, nombre, apellido, telefono, email);
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
            return;
        }

        // Auto-login después del registro
        Usuario registrado = usuarioDAO.login(emailLower, password);
        HttpSession session = req.getSession(true);
        session.setAttribute("usuario", registrado);
        session.setAttribute("toast", "¡Cuenta creada! Tienes 10 días para usar tu descuento del 5%.");
        resp.sendRedirect(req.getContextPath() + "/");
    }

    private void poblarFormulario(HttpServletRequest req, String nombre,
            String apellido, String telefono, String email) {
        req.setAttribute("fNombre",   nombre);
        req.setAttribute("fApellido", apellido);
        req.setAttribute("fTelefono", telefono);
        req.setAttribute("fEmail",    email);
    }
}
