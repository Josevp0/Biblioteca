package com.biblioteca.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String usuarioRol = (String) session.getAttribute("usuarioRol");
        
        // Verificar si el usuario está autenticado y es administrador
        if (usuarioRol == null || !"Administrador".equals(usuarioRol)) {
            response.sendRedirect("/login");
            return false;
        }
        
        return true;
    }
}
