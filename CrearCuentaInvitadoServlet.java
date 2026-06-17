package servlet;

import dao.UsuarioDAO;
import modelo.Pedido;
import modelo.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

/**
 * Permite al invitado crear cuenta post-compra con datos pre-llenados.
 * Se accede desde la página de confirmación del pedido.
 */
@WebServlet("/crear-cuenta")
public class CrearCuentaInvitadoServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Los datos del pedido vienen como parámetros en la URL
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

        if (!password.equals(confirmar) || password.length() < 6) {
            req.setAttribute("error", "Las contraseñas no coinciden o son muy cortas.");
            req.setAttribute("fNombre",   nombre);
            req.setAttribute("fApellido", apellido);
            req.setAttribute("fTelefono", telefono);
            req.setAttribute("fEmail",    email);
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
            return;
        }

        String emailLower = email.trim().toLowerCase();
        if (usuarioDAO.emailExiste(emailLower)) {
            req.getSession().setAttribute("toast", "Este correo ya tiene una cuenta. Inicia sesión.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        boolean ok = usuarioDAO.registrarDesdeInvitado(
                nombre.trim(), apellido.trim(),
                telefono != null ? telefono.trim() : null,
                emailLower, password);

        if (ok) {
            Usuario registrado = usuarioDAO.login(emailLower, password);
            req.getSession().setAttribute("usuario", registrado);
            req.getSession().setAttribute("toast",
                    "¡Cuenta creada! Tienes 10 días para usar tu descuento del 5%.");
            resp.sendRedirect(req.getContextPath() + "/historial");
        } else {
            req.setAttribute("error", "Error al crear la cuenta. Intenta nuevamente.");
            req.getRequestDispatcher("/jsp/usuario/registro.jsp").forward(req, resp);
        }
    }
}
