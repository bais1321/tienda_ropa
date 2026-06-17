package servlet;

import dao.UsuarioDAO;
import modelo.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Si ya está logueado, redirigir
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            Usuario u = (Usuario) session.getAttribute("usuario");
            resp.sendRedirect(req.getContextPath() + (u.isEsAdmin() ? "/admin/dashboard" : "/"));
            return;
        }
        req.getRequestDispatcher("/jsp/usuario/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Por favor completa todos los campos.");
            req.getRequestDispatcher("/jsp/usuario/login.jsp").forward(req, resp);
            return;
        }

        Usuario usuario = usuarioDAO.login(email.trim().toLowerCase(), password);

        if (usuario == null) {
            req.setAttribute("error", "Correo o contraseña incorrectos.");
            req.setAttribute("emailIngresado", email);
            req.getRequestDispatcher("/jsp/usuario/login.jsp").forward(req, resp);
            return;
        }

        // Crear sesión
        HttpSession session = req.getSession(true);
        session.setAttribute("usuario", usuario);
        session.setMaxInactiveInterval(30 * 60); // 30 minutos

        // Redirigir según rol
        String redirectUrl = (String) session.getAttribute("redirectUrl");
        session.removeAttribute("redirectUrl");

        if (usuario.isEsAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if (redirectUrl != null && !redirectUrl.isEmpty()) {
            resp.sendRedirect(redirectUrl);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
