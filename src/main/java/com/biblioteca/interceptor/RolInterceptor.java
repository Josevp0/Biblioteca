package com.biblioteca.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RolInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String usuarioRol = (String) session.getAttribute("usuarioRol");
        String requestURI = request.getRequestURI();
        
        // Si no hay sesión, redirigir al login
        if (usuarioRol == null) {
            response.sendRedirect("/login");
            return false;
        }
        
        // Verificar acceso según el rol
        if (requestURI.startsWith("/admin/") && !"Administrador".equals(usuarioRol)) {
            response.sendRedirect("/acceso-denegado");
            return false;
        }
        
        if (requestURI.startsWith("/bibliotecario/") && !"Bibliotecario".equals(usuarioRol)) {
            response.sendRedirect("/acceso-denegado");
            return false;
        }
        
        if (requestURI.startsWith("/auxiliar/") && !"Auxiliar".equals(usuarioRol)) {
            response.sendRedirect("/acceso-denegado");
            return false;
        }
        
        if (requestURI.startsWith("/profesor/") && !"Profesor".equals(usuarioRol)) {
            response.sendRedirect("/acceso-denegado");
            return false;
        }
        
        if (requestURI.startsWith("/estudiante/") && !"Estudiante".equals(usuarioRol)) {
            response.sendRedirect("/acceso-denegado");
            return false;
        }
        
        return true;
    }
}
