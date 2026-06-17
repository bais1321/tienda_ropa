package filtro;

import modelo.Usuario;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Filtro de seguridad que protege rutas de admin y de usuario.
 * Se aplica automáticamente por anotaciones.
 */
@WebFilter(urlPatterns = {"/admin/*", "/historial", "/perfil"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        boolean esRutaAdmin = uri.contains("/admin/");

        if (esRutaAdmin) {
            // Rutas /admin/* requieren sesión de administrador
            if (usuario == null || !usuario.isEsAdmin()) {
                session = request.getSession();
                session.setAttribute("mensajeError", "Debes iniciar sesión como administrador.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        } else {
            // Rutas /historial, /perfil requieren sesión de usuario
            if (usuario == null) {
                session = request.getSession();
                session.setAttribute("mensajeError", "Debes iniciar sesión para continuar.");
                session.setAttribute("redirectUrl", uri);
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig fc) {}
    @Override public void destroy() {}
}
